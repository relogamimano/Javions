# Javions
## Live flight tracker (Flightradar24 from scratch)
In order to facilitate air traffic control, airplanes—and other aircraft such as helicopters, balloons, etc.—continuously broadcast digital radio messages called "ADS-B messages" (for Automatic Dependent Surveillance—Broadcast). These messages communicate a lot of information about their sender, such as their identity, position, speed, direction of travel, etc.

ADS-B messages are transmitted on the 1090 MHz frequency and can be received either by a specialized receiver or by a simple "software-defined radio" (SDR in English), connected to a computer running a program capable of decoding these messages.

The goal of this year's project, called Javiones, is precisely to write a program capable of decoding ADS-B messages received by a software-defined radio, and to display the aircraft that have transmitted them on a map. The figure below shows the graphical interface of the completed project.

![image](https://github.com/user-attachments/assets/154b3218-2260-4660-bfe4-4d74b85d124d)

The software radio used for Javions is the AirSpy R2, visible below. Like any software radio, it must be connected to an antenna and a computer. Once set to a given frequency, it digitizes the radio signal it receives from the antenna and then transmits it to the computer.

![image](https://github.com/user-attachments/assets/d0c69e74-95ce-43ce-a4ee-0f6e15263279)


In order to receive ADS-B messages from an aircraft, there must be no significant obstacle between it and the receiving antenna. It is therefore important to position the latter well, and the image in Figure 1 was for example obtained with an antenna placed near the roof of a building in the center of Lausanne.

That said, even in good reception conditions, the curvature of the Earth means that it is not possible to receive messages from aircraft located more than a few hundred kilometers from the receiver. Javions will therefore only be able to display aircraft located around Lausanne.

To cover a larger geographical area and allow aircraft tracking over a long distance, it is possible to collect, via the Internet, ADS-B messages received by a large number of radios distributed on Earth. This is what several message collection sites do, managed either by aviation enthusiasts (e.g. ADSB.lol, adsb.fi or ADSBHub), researchers (The OpenSky Network), activists (Dictator Alert) or commercial companies (ADS-B Exchange, flightradar24, planefinder, FlightAware, etc.). For the sake of simplicity, however, we will not interact with these sites in the context of this project.
