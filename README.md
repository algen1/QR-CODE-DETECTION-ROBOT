# QR Code Detection Robot

A Java program for the SwiftBot, a Raspberry Pi-based robot. Scanning a QR code with the robot's camera selects a behaviour mode, and the robot then uses its ultrasonic sensor to find nearby objects and react to them.

Each time it detects an object, the robot takes a photo and adds an entry to a log file.

## Modes

The text in the QR code selects the mode and must match one of these values exactly:

| QR code text | Underlights | Behaviour when an object is within 100 cm |
| --- | --- | --- |
| `Curious SwiftBot` | Green | Keeps a 30 cm gap: moves forward if further away, reverses if closer, and flashes its lights three times at exactly 30 cm |
| `Scaredy SwiftBot` | Red | Reverses for 3 seconds if the object is closer than 50 cm |
| `Dubious SwiftBot` | Green or red | Randomly becomes Curious or Scaredy |

## How it works

1. **Choose a mode:** the robot has 10 seconds to find a QR code with a supported value. An unrecognised code clears the current mode.
2. **Start detection:** the underlights turn blue and the robot starts taking ultrasonic distance readings.
3. **Respond to readings:** if an object is within 100 cm, the robot reacts according to its mode, saves a photo and logs the distance. Otherwise, it waits 5 seconds and moves forward for 1 second.
4. **Stop:** pressing **Button X** stops the robot and returns to the menu.

## Requirements

- SwiftBot with a Raspberry Pi, camera and ultrasonic sensor
- I2C and the camera enabled on the Raspberry Pi
- JDK 23 (the version used for the compiled classes in this repo)
- Maven
- Access to Brunel University's SwiftBot Maven repository, which provides `SwiftBot-API` 5.1.3

## Getting started

Enable I2C and the camera if you haven't already, then reboot:

```bash
sudo raspi-config nonint do_i2c 0
sudo raspi-config nonint do_camera 0
sudo reboot
```

Build the project from the folder containing `pom.xml`:

```bash
cd SwiftBotAlgen/SwiftBotAlgen
mvn compile dependency:copy-dependencies -Dmaven.compiler.release=23
```

Run the main program:

```bash
java -cp "target/classes:target/dependency/*" SwiftBotAlgen.QRCodeDetection
```

You'll then see this menu:

```text
1 = Scan QR Code for Mode Selection
2 = Start Object Detection
3 = View Execution Log
0 = Exit
```

Scan a QR code with option 1 before starting detection with option 2.

## Hardware test

`DoesMySwiftBotWork` is a separate menu-driven tool for checking the camera, ultrasonic sensor, wheels, buttons, lights and QR code scanning:

```bash
java -cp "target/classes:target/dependency/*" DoesMySwiftBotWork
```

## Output files

| File | Contents |
| --- | --- |
| `/data/home/pi/object_detected.png` | The latest photo, 720×720 pixels |
| `/data/home/pi/swiftbot_log.txt` | Detection distances and stop events |

## Project structure

```text
SwiftBotAlgen/SwiftBotAlgen/
├── pom.xml
└── src/main/java/
    ├── DoesMySwiftBotWork.java        # Hardware test tool
    └── SwiftBotAlgen/
        └── QRCodeDetection.java       # Main program
```

## Known limitations

- Each new photo overwrites the previous one.
- Output file paths are hardcoded.
- Dubious mode keeps its first random choice until another QR code is scanned.
- The ultrasonic sensor measures distance only, so the robot can't identify what an object is.

## Tech

Java · Maven · SwiftBot API · Raspberry Pi
