# Monoprice 10761 Multizone Amp Binding

Monoprice sells a multizone amplifier (https://www.monoprice.com/product?p_id=10761) for building
a 6x6 matrix, expandable to a 6x18 matrix. This binding connects to the amplifier over a serial
connection to allow status updates and control of all zones.

## Supported Things

This binding supports the following Thing types

| Thing      | Thing Type | Description                                                            |
|------------|------------|------------------------------------------------------------------------|
| amp        | Bridge     | The RS-232 interface.                                                  |
| zone       | Thing      | Represents a single output zone.                                       |

## Discovery

The bridge will need to be manually added in the discovery inbox.
After a bridge is discovered and available to openHAB, the binding will attempt to discover how many
amps you have stacked together, and add things for all zones to the discovery inbox.

## Thing Configuration

Monoprice 10761 things can be configured either through the online configuration utility via discovery, or manually through the 'monoprice107671.things' configuration file.
The following table shows the available configuration parameters for each thing.


## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```ESH-INF/thing``` of your binding._

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._
