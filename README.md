Create: Fluid and Fixins
=======
Current Version: 0.0.1 (pre-release development)
Last README Update: 5/13/2026


Mod Description:
==========
Adds new fluids and corresponding blocks to match the design and usability of _Create Aeronautics_' Levitite. _Create: Fluids and Fixins_ extends Levitite into a family of unique fluids/blocks, including three planned additions to the block family and six planned fluids with unique acquisitions, interactions, and uses. Additional _Create_ and _Create Aeronautics_ compatible Contraptions are also planned, including new Sails.


Blocks:
1) Densite
2) Propulsite
3) Oscillite


Fluids:
1) Void Sea Slurry
2) Densite Emulsion
3) Drift Condensate
4) Propulsite Flurry
5) Soul Steep
6) Oscillite Suspension


Additional Resources: 
==========
[Mojang License Reference](https://github.com/NeoForged/NeoForm/blob/main/Mojang.md)
[NeoForge Community Documentation](https://docs.neoforged.net/)
[NeoForged Discord](https://discord.neoforged.net/)
[Create Repository](/)
[Create Aeronautics Repository](/)


Development Setup:
==========
1. Install [IntelliJ IDEA](https://www.jetbrains.com/idea/download/?section=windows)
    - Update PATH variable (restart needed) := CHECK Add "bin" folder to the PATH
2. Install [Temurin jdk-21.0.11+10](https://adoptium.net/temurin/releases/?version=21&os=windows&arch=any&mode=filter)
    - Set or override JAVA_HOME variable := Will be installed on local hard drive
3. Clone repository
4. Set "Project Structure... > Project Settings > Project > Language Level" := 21
5. Set "Project Structure... > Project Settings > Project > SDK" := temurin-21
6. Set "Settings... > Build, Execution, Deployment > Build Tools > Gradle > Gradle JVM" := Project SDK


If at any point you are missing libraries in your IDE, or you've run into problems you can
run `gradlew --refresh-dependencies` to refresh the local cache. `gradlew clean` to reset everything 
{this does not affect your code} and then start the process again.