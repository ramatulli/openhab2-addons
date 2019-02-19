# IntesisBox Binding

This binding connects to WiFi [IntesisBox](http://www.intesisbox.com/) devices, speaking an ASCII protocol.
It does _not_ support [IntesisHome](http://www.intesishome.com/) devices.

## Supported Things

This binding has only been tested against a FJ-RC-WMP-1 IntesisBox.

## Discovery

Discovery is done via a proprietary UDP broadcast. Discovered things will be added to the inbox and configured by MAC address.
You can also configure a static IP on the individual thing, or manually add a thing via IP address.

## Thing Configuration

_Describe what is needed to manually configure a thing, either through the (Paper) UI or via a thing-file. This should be mainly about its mandatory and optional configuration parameters. A short example entry for a thing file can help!_

_Note that it is planned to generate some part of this based on the XML files within ```ESH-INF/thing``` of your binding._

## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```ESH-INF/thing``` of your binding._

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
