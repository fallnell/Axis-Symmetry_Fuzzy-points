plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":jftk"))
    implementation(project(":jflib"))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // https://mvnrepository.com/artifact/com.panayotis/javaplot
    implementation("com.panayotis:javaplot:0.5.0")

}

tasks.test {
    useJUnitPlatform()
}