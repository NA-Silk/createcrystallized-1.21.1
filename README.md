# Create: Crystallized
*Current Version: 0.0.1 (pre-release development)*

*Last README Update: 9/23/2026*


## Mod Description:
Adds new fluids, blocks, and items inspired by *Create Aeronautics*' features, including the Levitite and Magnet blocks.

*Create: Crystallized* (CC) extends Levitite into a family of unique fluids/blocks, including three primary additions to the block family and 5 new fluids with unique acquisitions, interactions, and uses. Additionally, CC will add a series of new crafting recipes, related items, and advanced "Machined" blocks to take Aeronautics builds to the next level. 

**Main Series Blocks**:
1. Densite - Heavy; sensitive to Redstone. 
2. Propulsite - Light; careful, it might try to run away. 
3. Oscillite - Good vibrations; really doesn't like being tipped over. 

**Machined Blocks**
1. Densite Well - A variable gravity well that pulls in Simulated Contraptions. Attracts Simulated Contraptions within a scalable radius, area, and pull strength when power is supplied. 
2. Propusite Thruster - Powerful burst of propulsion with the power of crystals. Begins to charge at high velocity, loses charge when not moving fast enough, and releases a powerful burst of thrust when powered. 
3. Oscillite Cannon - Violent resonance in one convenient package (not suitable for home defence). Charges by absorbing nearby Echo Crystalls; releases a immensely powerful beam of focused sound waves to tear through blocks and entities alike when powered. 

**Fluids**:
1. Void Sea Slurry - Find some hiding in the End Sea. 
2. Densite Emulsion - Refined slurry with high density and a cold and spacey vibe. 
3. Drift Condensate - Find it high in the sky. Caution: it may try to go back. 
4. Propulsite Flurry - Refined condensate with lightning quick flow. 
5. Oscillite Suspension - Liquid sound? Very sensistive to additional noises. 

Additional *Create* and *Create Aeronautics* compatible Contraptions are also added, including a new shovel for paddling your (air)ships! 


## Additional Resources:
[Mojang License Reference](https://github.com/NeoForged/NeoForm/blob/main/Mojang.md)

[NeoForge Community Documentation](https://docs.neoforged.net/)

[NeoForged Discord](https://discord.neoforged.net/)

[Create Repository](https://github.com/Creators-of-Create/Create)

[Create Aeronautics Repository](https://github.com/Creators-of-Aeronautics/Simulated-Project)

[Sable](https://github.com/ryanhcode/sable)


## Development Setup:
1. Install [IntelliJ IDEA](https://www.jetbrains.com/idea/download/?section=windows)
    - Update PATH variable (restart needed) := CHECK Add "bin" folder to the PATH
2. Install [Temurin jdk-21.0.11+10](https://adoptium.net/temurin/releases/?version=21&os=windows&arch=any&mode=filter)
    - Set or override JAVA_HOME variable := Will be installed on local hard drive
3. Clone repository
4. Set "Project Structure... > Project Settings > Project > Language Level" := 21
5. Set "Project Structure... > Project Settings > Project > SDK" := temurin-21
6. Set "Settings... > Build, Execution, Deployment > Build Tools > Gradle > Gradle JVM" := Project SDK


>NeoForge Note:
>
>If at any point you are missing libraries in your IDE, or you've run into problems you can run `gradlew --refresh-dependencies` to refresh the local cache or `gradlew clean` to reset everything {this does not affect your code} and then start the process again.
