# Battleship

## About

Battleship is a strategy-type guessing video game for two players, developed in Kotlin for the Android operating system.
It is known as a pencil-and-paper game and is played on a ruled grid (paper or board) where players mark the locations of their fleets of warships.
The locations of the ships are concealed from the other player.
Players alternate turns targeting each other's ships, and the objective of the game is to destroy the opposing player's fleet.

### Game Modes

- **Play Against Bot**: Challenge a computer-controlled opponent.
- **Local Multiplayer**: Compete against another player on the same device.
- **Bluetooth Multiplayer**: Play against another player via Bluetooth connection.

## Download and Install

Download the application [here](https://github.com/SavaMijailovic/battleship/releases) on your Android device and install it by opening the downloaded APK file.

## Build and Run

**Requirements: Java Development Kit (JDK), Android SDK, Android 8.0 or higher**

The application is developed in Kotlin for the Android operating system using IntelliJ IDEA.
To build, install, and run the application on your device you can use IDE or command line.
To install and run, you need to be connected to an Android device via USB or Wi-Fi.
Ensure that USB (Wireless) debugging and Install via USB options are enabled in the Developer options of your device settings.

- Clone the repository locally and navigate to the created directory:
    ```sh
    git clone https://github.com/SavaMijailovic/battleship.git
    cd battleship
    ```

- Using IDE
    - Open project (directory) and find the Run option
    - You can also run it on a virtual device

- Using Command Line
    - On Linux or Mac:
        ```sh
        ./gradlew installDebug
        ```
    - On Windows:
        ```sh
        gradlew installDebug
        ```

It should build and install the application on the connected device, and then you should be able to run it by opening it.

## Authors
- [Sava Mijailovic](https://github.com/SavaMijailovic)
- [Dimitrije Jovanovic](https://github.com/dimitrije-24)

## Awards
- [Award for the best project](./award.pdf)
