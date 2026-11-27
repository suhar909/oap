#!/usr/bin/env node
/**
 * Build script that attempts to compile the Aim Assist Fabric mod
 * by checking for Java/Gradle availability or downloading them.
 */

import { execSync, exec } from 'child_process';
import { existsSync, mkdirSync, writeFileSync, chmodSync, createWriteStream } from 'fs';
import { join } from 'path';
import https from 'https';
import http from 'http';

const ROOT = process.cwd();

function run(cmd, opts = {}) {
    console.log(`> ${cmd}`);
    try {
        const result = execSync(cmd, { 
            encoding: 'utf-8', 
            cwd: ROOT,
            stdio: 'pipe',
            timeout: 300000,
            ...opts 
        });
        console.log(result);
        return result;
    } catch (e) {
        console.error(`Command failed: ${e.message}`);
        if (e.stdout) console.log(e.stdout);
        if (e.stderr) console.error(e.stderr);
        return null;
    }
}

function download(url, dest) {
    return new Promise((resolve, reject) => {
        console.log(`Downloading: ${url}`);
        const file = createWriteStream(dest);
        const proto = url.startsWith('https') ? https : http;
        
        const request = proto.get(url, { headers: { 'User-Agent': 'Mozilla/5.0' } }, (response) => {
            // Handle redirects
            if (response.statusCode >= 300 && response.statusCode < 400 && response.headers.location) {
                file.close();
                download(response.headers.location, dest).then(resolve).catch(reject);
                return;
            }
            
            if (response.statusCode !== 200) {
                reject(new Error(`HTTP ${response.statusCode}`));
                return;
            }
            
            response.pipe(file);
            file.on('finish', () => {
                file.close();
                console.log(`Downloaded: ${dest}`);
                resolve();
            });
        });
        
        request.on('error', (err) => {
            file.close();
            reject(err);
        });
        
        request.setTimeout(120000, () => {
            request.destroy();
            reject(new Error('Timeout'));
        });
    });
}

async function main() {
    console.log('=== Aim Assist Mod Builder ===\n');
    
    // Step 1: Check Java
    console.log('--- Step 1: Checking Java ---');
    let javaVersion = run('java -version 2>&1');
    
    if (!javaVersion) {
        console.log('Java not found. Trying to install...');
        
        // Try apt-get (Linux)
        let installed = run('apt-get update && apt-get install -y default-jdk 2>&1');
        if (!installed) {
            // Try with sudo
            installed = run('sudo apt-get update && sudo apt-get install -y default-jdk 2>&1');
        }
        
        if (!installed) {
            // Try downloading Adoptium/Temurin JDK 21
            console.log('Trying to download JDK 21...');
            const jdkUrl = 'https://api.adoptium.net/v3/binary/latest/21/ga/linux/x64/jdk/hotspot/normal/eclipse';
            const jdkPath = join(ROOT, 'jdk21.tar.gz');
            
            try {
                await download(jdkUrl, jdkPath);
                run(`tar -xzf ${jdkPath} -C ${ROOT}`);
                
                // Find the extracted JDK directory
                const dirs = execSync(`ls -d ${ROOT}/jdk-21* 2>/dev/null || ls -d ${ROOT}/temurin-21* 2>/dev/null`, { encoding: 'utf-8' }).trim().split('\n');
                if (dirs[0]) {
                    process.env.JAVA_HOME = dirs[0];
                    process.env.PATH = `${dirs[0]}/bin:${process.env.PATH}`;
                    console.log(`Using JDK from: ${dirs[0]}`);
                }
            } catch (e) {
                console.error('Failed to download JDK:', e.message);
            }
        }
        
        // Check again
        javaVersion = run('java -version 2>&1');
        if (!javaVersion) {
            console.error('\n❌ FAILED: Could not install Java. Please install JDK 21 manually.');
            console.error('   Download from: https://adoptium.net/');
            process.exit(1);
        }
    }
    
    console.log('✅ Java found!');
    
    // Step 2: Check/Setup Gradle
    console.log('\n--- Step 2: Setting up Gradle ---');
    
    const gradlewPath = join(ROOT, 'gradlew');
    if (existsSync(gradlewPath)) {
        chmodSync(gradlewPath, '755');
    }
    
    // Check if gradle wrapper jar exists
    const wrapperJar = join(ROOT, 'gradle', 'wrapper', 'gradle-wrapper.jar');
    if (!existsSync(wrapperJar)) {
        console.log('Gradle wrapper JAR not found. Downloading...');
        mkdirSync(join(ROOT, 'gradle', 'wrapper'), { recursive: true });
        
        try {
            await download(
                'https://raw.githubusercontent.com/gradle/gradle/v8.12.0/gradle/wrapper/gradle-wrapper.jar',
                wrapperJar
            );
        } catch (e) {
            // Try alternative URL
            try {
                await download(
                    'https://services.gradle.org/distributions/gradle-8.12-bin.zip',
                    join(ROOT, 'gradle-8.12-bin.zip')
                );
                console.log('Extracting Gradle...');
                run(`unzip -o ${join(ROOT, 'gradle-8.12-bin.zip')} -d ${ROOT}`);
                process.env.PATH = `${ROOT}/gradle-8.12/bin:${process.env.PATH}`;
            } catch (e2) {
                console.error('Failed to get Gradle wrapper:', e2.message);
                // Try installing gradle via apt
                run('apt-get install -y gradle 2>&1 || sudo apt-get install -y gradle 2>&1');
            }
        }
    }
    
    // Step 3: Build the mod
    console.log('\n--- Step 3: Building the mod ---');
    
    let buildResult;
    if (existsSync(gradlewPath) && existsSync(wrapperJar)) {
        buildResult = run('./gradlew build --no-daemon --stacktrace 2>&1');
    } else {
        // Try system gradle
        buildResult = run('gradle build --no-daemon 2>&1');
    }
    
    if (buildResult && existsSync(join(ROOT, 'build', 'libs', 'aim-assist-1.0.0.jar'))) {
        console.log('\n✅ BUILD SUCCESSFUL!');
        console.log(`📦 JAR: ${join(ROOT, 'build', 'libs', 'aim-assist-1.0.0.jar')}`);
    } else {
        console.log('\n❌ Build may have issues. Check output above.');
        // List what's in build/libs if it exists
        if (existsSync(join(ROOT, 'build', 'libs'))) {
            const files = execSync(`ls -la ${join(ROOT, 'build', 'libs')}`, { encoding: 'utf-8' });
            console.log('Files in build/libs:', files);
        }
    }
}

main().catch(e => {
    console.error('Fatal error:', e);
    process.exit(1);
});
