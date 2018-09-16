# OraKuraGateway
This repository contains the code for the integration between Eclipse Kura and Oracle IoT Gateway.

Eclipse Kura is a framework developed as Eclipse Project to assist in the development
of IoT solutions based on Gateway.

With Kura you can manage remotely the Gateway (network, firewall) and deploy from remore application code.
Code is developed and run inside Kura as OSGi Bundles.

What I have developed is an integration between Eclipse Kura and Oracle IoT Cloud.

The integration is a OSGi bundle, develped as a configurable component, that
subscribe to an MQTT topic (use internal MQTT broker) and for each message received it transform the message
and send it to Oracle IoT.

It is configurable, severable DeviceModel are supported (OBD2,  Aircare..).

The integration has been tested on a RPI 3 B or B+, with Raspian Jessie and the latest Stretch.
Tested with Oracle JDK 8.

It should be considered as a starting point to develop production code.

See License.
