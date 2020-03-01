# IntesisBox Binding

This binding connects to WiFi [IntesisBox](http://www.intesisbox.com/) devices, speaking an ASCII protocol.
It does _not_ support [IntesisHome](http://www.intesishome.com/) devices. This binding has only been tested against a FJ-RC-WMP-1 IntesisBox.

[update 29-Feb-2020]

Binding tested against IntesisBox model FJ-AC-WMP-1 connected to a Fujitsu AUTGxxKRLA. Some further enhancement made to the binding include
- addition of the IntesisBox ID command which returns details about the intesisbox device itself (rather than the connected AC unit) 
- channels MODE, FANSP, VANEUD, VANELR now dynamically set based on the LIMITS returned from the connected A/C unit. 

If the channel is not supported by the A/C unit, such as VALUELR on a ceiling cassette unit, it will maintain the the default values for that channel, e.g. [AUTO, 1-9, SWING, PULSE]. Just manually unlink the channel if not used in the OpenHab thing configuration.


## Supported Things

This binding only supports one thing type:

| Thing      | Thing Type | Description                                                            |
|------------|------------|------------------------------------------------------------------------|
| intesisbox | Thing      | Represents a single thermostat                                         |

## Discovery

Discovery is done via a proprietary UDP broadcast. Discovered things will be added to the inbox and configured by MAC address.
You can also configure a static IP on the individual thing, or manually add a thing via IP address.

## Thing Configuration

You can manually configure an intesisbox thing. If you provide only a MAC Address, it will go through the discovery
process to determine its current IP before connecting. Alternately, you can provide an IP Address for direct connection
without discovery.

## Channels

| Channel ID | Item Type          | Description                                                           | Possible Values |
|------------|--------------------|-----------------------------------------------------------------------|-|
| onoff      | Switch             | Turns power on/off for your climate system.                           | ON, OFF |
| mode       | String             | The heating/cooling mode.                                             | AUTO, HEAT, COOL, DRY, FAN |
| setptemp   | Number:Temperature | The currently set target temperature.                                 | |
| ambtemp    | Number:Temperature | (Readonly) The ambient air temperature.                               | |
| fansp      | String             | Fan speed (if applicable)                                             | AUTO, 1-9 |
| vaneud     | String             | Control of up/down vanes (if applicable)                              | AUTO, 1-9, SWING, PULSE |
| vanelr     | String             | Control of left/right vanes (if applicable)                           | AUTO, 1-9, SWING, PULSE |
| errstatus  | String             | (Readonly) A description of an error coming from your climate system. | |
| errcode    | Number             | (Readonly) An error code coming from your climate system.             | |
| model      | String             | (Readonly) The Intesis device model reference                         | FJ-AC-WMP-1|
| mac        | String             | (Readonly) The 6 bytes of the MAC address                             | CC3F1D01EAB9|
| ip         | String             | (Readonly) The IP address of the IntesisBox                           | 192.168.1.100|
| protocol   | String             | (Readonly) The external protocol supported                            | ASCII|
| version    | String             | (Readonly) The firmware version running in the device                 | v1.3.3|
| rssi       | String             | (Readonly) The received Signal Strength Indication for the Wi-Fi      | -55|
| name       | String             | (Readonly) The host name of the IntesisBox                            | WMP_01EAB9|



Note that individual A/C units may not support all channels, or all possible values for those channels.
For example, not all A/C units have controllable vanes. Or fan speed may be limited to 1-4, instead of all of 1-9.
The set point temperature is also limited to a device specific range. For set point temperature, sending an invalid value
will cause it to choose the minimum/maximum allowable value as appropriate. The device will also round it to
whatever step size it supports. For all other channels, invalid values
are ignored.

## Full Example

The binding can be fully setup from the Paper UI but if you decide to use files here is a full example:

Things

```intesisbox.things
Thing intesisbox:intesisbox:WMP_0001 "AC Unit Adapter" @ "AC" [ipAddress="192.168.1.100", port=3310]
```

Items

```intesisbox.items
Switch ac               "Power"                                         { channel="intesisbox:intesisbox:WMP_0001:onoff" }
String acMode           "Mode"                                          { channel="intesisbox:intesisbox:WMP_0001:mode" }
Number acSetPoint       "Set Temperature [%.1f °C]" <heating>           { channel="intesisbox:intesisbox:WMP_0001:setptemp" }
Number acAmbientTemp    "Current Temperature [%.1f °C]" <temperature>   { channel="intesisbox:intesisbox:WMP_0001:ambtemp" }
String acFanSpeed       "Fan Speed" <fan>                               { channel="intesisbox:intesisbox:WMP_0001:fansp" }

String acVanesUpDown    "Vanes U/D Position"                            { channel="intesisbox:intesisbox:WMP_0001:vaneud" }
String acVanesLeftRight "Vanes L/R Position"                            { channel="intesisbox:intesisbox:WMP_0001:vanelr" }

String acModel          "Model"                                         { channel="intesisbox:intesisbox:WMP_0001:model" }
String acMac            "MAC Address"                                   { channel="intesisbox:intesisbox:WMP_0001:mac" }
String acIp             "IP Address"                                    { channel="intesisbox:intesisbox:WMP_0001:ip" }
String acProtocol       "Protocol"                                      { channel="intesisbox:intesisbox:WMP_0001:protocol" }
String acVersion        "FW Version"                                    { channel="intesisbox:intesisbox:WMP_0001:version" }
String acRssi           "WiFi RSSI"                                     { channel="intesisbox:intesisbox:WMP_0001:rssi" }
String acName           "Host Name"                                     { channel="intesisbox:intesisbox:WMP_0001:name" }

String acErrorStatus    "Error Status"                                  { channel="intesisbox:intesisbox:WMP_0001:errstatus" }
Number acErrorCode      "Error Code"                                    { channel="intesisbox:intesisbox:WMP_0001:errcode" }

String blank            ""
```

Sitemap


```intesisbox.sitemap
sitemap intesisbox label="My Home Automation Testing" {

    Frame item=acModel label="Fujitsu A/C via IntesisBox" {
        Text item=blank icon=""
    }

    Frame label="Controller" {

        Switch item=ac

        Selection item=acMode icon="settings" valuecolor=["COOL"="aqua", "HEAT"="red", "AUTO"="orange"]
        Setpoint item=acSetPoint minValue=18 maxValue=30 step=0.5 icon="heating" valuecolor=[>=26="orange", <=21="aqua"]
        Text item=acAmbientTemp icon="temperature"
        Selection item=acFanSpeed 
        Text item=blank icon=""

        Selection item=acVanesUpDown
        Selection item=acVanesLeftRight visibility=[acVanesLeftRight!="SWING"]
    }

    Frame label="Diagnostic" {

        Text item=acModel
        Text item=acMac
        Text item=acIp
        Text item=acProtocol
        Text item=acVersion
        Text item=acRssi
        Text item=acName 
        Text item=blank icon=""
        Text item=acErrorStatus valuecolor=["OK"="green", "ERR"="red"]
        Text item=acErrorCode valuecolor=[0="green", >=1="red"]

    }
}
```


