# Javions
## Live flight tracker (Flightradar24 from scratch)

Java program capable of decoding ADS-B messages received by a software-defined radio, and to display the aircraft that have transmitted them on a map. The figure below shows the graphical interface of the completed project.

![image](https://github.com/user-attachments/assets/154b3218-2260-4660-bfe4-4d74b85d124d)

The software radio used for Javions is the AirSpy R2, visible below. Like any software radio, it must be connected to an antenna and a computer. Once set to a given frequency, it digitizes the radio signal it receives from the antenna and then transmits it to the computer.

![image](https://github.com/user-attachments/assets/d0c69e74-95ce-43ce-a4ee-0f6e15263279)

## Motivation
In order to facilitate air traffic control, airplanes—and other aircraft such as helicopters, balloons, etc.—continuously broadcast digital radio messages called "ADS-B messages" (for Automatic Dependent Surveillance—Broadcast). These messages communicate a lot of information about their sender, such as their identity, position, speed, direction of travel, etc.

ADS-B messages are transmitted on the 1090 MHz frequency and can be received either by a specialized receiver or by a simple "software-defined radio" (SDR in English), connected to a computer running a program capable of decoding these messages.

In order to receive ADS-B messages from an aircraft, there must be no significant obstacle between it and the receiving antenna. It is therefore important to position the latter well.

That said, even in good reception conditions, the curvature of the Earth means that it is not possible to receive messages from aircraft located more than a few hundred kilometers from the receiver. Javions will therefore only be able to display aircraft located around Lausanne.

To cover a larger geographical area and allow aircraft tracking over a long distance, it is possible to collect, via the Internet, ADS-B messages received by a large number of radios distributed on Earth. This is what several message collection sites do, managed either by aviation enthusiasts (e.g. ADSB.lol, adsb.fi or ADSBHub), researchers (The OpenSky Network), activists (Dictator Alert) or commercial companies (ADS-B Exchange, flightradar24, planefinder, FlightAware, etc.). For the sake of simplicity, however, we will not interact with these sites in the context of this project.

## Specifications and technical details
### 1. OpenStreetMap
The map on which we will display the aircraft comes from the OpenStreetMap project — often abbreviated OSM —, which aims to create a geographic database of the entire world, freely usable and modifiable. The idea is therefore similar to that of Wikipedia, but for geographic data.

The data from OpenStreetMap can be used in different ways, among others to draw a map like the one on which we will display the aircraft, visible below.

### 2. WGS 84 coordinates
To be able to display an aircraft on the map, you need to know where it is in space, and therefore have a way to represent its position anywhere in the vicinity of the Earth. For this, we will use geographic coordinates, which represent the position of a point by means of two angles:

longitude, which is the angle between the point and the prime meridian—a meridian being a semicircle connecting the two poles,
latitude, which is the angle between the point and the equator.

These coordinates are generally specified in the system called WGS 84 (World Geodetic System, version 1984), used among others by GPS receivers.

In this system, longitude is between -180° and +180°, with longitude 0 being that of the prime meridian, which passes near the Greenwich Observatory in England. The latitude of a point is between -90° and +90°, with latitude 0 being that of the equator. The figure below illustrates these conventions.

![image](https://github.com/user-attachments/assets/54a1d7eb-1022-4e75-af98-14dfb1e64d37)

Geographic coordinates are an example of what are called spherical coordinates, which can be seen as a three-dimensional generalization of polar coordinates. Such coordinates are particularly well suited to describing the position of points on the surface of a sphere. Since the Earth is (almost) spherical, it is not surprising that this type of coordinate has become established.

### 3. Web Mercator Projection
The fact that the Earth is spherical means that it is not directly possible to represent it on the flat surface of a map. It is therefore necessary to project it onto the plane, using a map projection.

The projection used by OpenStreetMap is generally known as Web Mercator. As its name suggests, it is a variant of the famous Mercator projection, frequently used for world maps. The image below shows a large part of the Earth projected using this projection.

### 4. Zoom Levels
Unlike physical maps printed on paper, which are drawn at a fixed scale, electronic maps such as OpenStreetMap are available at multiple scales. When viewing such maps, one can freely switch between scales, for example by using the buttons labeled + and -.

OpenStreetMap calls the different scales zoom levels. Level 0 corresponds to the largest scale at which the map is available.

At zoom level 1, the OpenStreetMap world map is exactly twice as large as at zoom level 0, in each dimension. This is therefore an image measuring 512 (29) pixels on a side, visible below.

### 5. Projection formulas
In order to be able to place a point on a map whose geographic coordinates are known, it is necessary to know the projection formulas. These formulas give the Cartesian coordinates (x, y) of a point on the map at the zoom level z, as a function of its geographic coordinates (λ, φ), expressed in radians (!):
![image](https://github.com/user-attachments/assets/9252bea8-577f-4236-843b-c09f656c41a4)

## Requirements
- Have Java 17 installed
### JUnit
JUnit is a Java library that simplifies writing unit tests. To use it in an IntelliJ project, you need to add it to the project by doing the following:

- Select the Project Structure… entry from the File menu.
- Select Libraries under Project Settings, click the + button in the second column, and choose From Maven…
- In the dialog that opens, enter the following text, which is the so-called “Maven coordinates” for Junit version 5.9.2: org.junit.jupiter:junit-jupiter:5.9.2
- Click the Ok button.
- If all went well, you should see JUnit appear under External Libraries in the Project panel.

### JavaFX
Follow this setup guide : https://cs108.epfl.ch/archive/23/g/openjfx.html
