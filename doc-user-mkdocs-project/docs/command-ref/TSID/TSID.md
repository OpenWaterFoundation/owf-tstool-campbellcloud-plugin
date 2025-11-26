# TSTool / Command / TSID for Campbell Cloud #

*   [Overview](#overview)
*   [Command Editor](#command-editor)
*   [Command Syntax](#command-syntax)
*   [Examples](#examples)
*   [Troubleshooting](#troubleshooting)
*   [See Also](#see-also)

-------------------------

## Overview ##

The TSID command for Campbell Cloud causes a single time series to be read from Campbell Cloud web services using default parameters.
A TSID command is created by copying a time series from the ***Time Series List*** in the main TSTool interface
to the ***Commands*** area.
TSID commands can also be created by editing the command file with a text editor.

See the [Campbell Cloud Datastore Appendix](../../datastore-ref/CampbellCloud/CampbellCloud.md) for information about TSID syntax
and defaults.

See also the [`ReadCampbellCloud`](../ReadCampbellCloud/ReadCampbellCloud.md) command,
which reads one or more time series and provides parameters for control over how data are read.

If the input period is not specified with the
[`SetInputPeriod`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/SetInputPeriod/SetInputPeriod/) command,
the default is to read the last 30 days.
If a period is specified but the time zone is not specified, the time zone for the computer is used.

Use the TSTool time series list to see the time series identifier corresponding to each time series.

## Command Editor ##

All TSID commands are edited using the general
[`TSID`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/TSID/TSID/)
command editor.

## Command Syntax ##

See the [Campbell Cloud Datastore Appendix](../../datastore-ref/CampbellCloud/CampbellCloud.md) for information about TSID syntax.

## Examples ##

See the [automated tests](https://github.com/OpenWaterFoundation/owf-tstool-campbellcloud-plugin/tree/main/test/commands/TSID/).

## Troubleshooting ##

*   See the [`ReadCampbellCloud` command troubleshooting](../ReadCampbellCloud/ReadCampbellCloud.md#troubleshooting) documentation.

## See Also ##

*   [`ReadCampbellCloud`](../ReadCampbellCloud/ReadCampbellCloud.md) command for full control reading Campbell Cloud time series
*   [`ReadTimeSeries`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/ReadTimeSeries/ReadTimeSeries/) command - provides more flexibility than a TSID
*   [`SetInputPeriod`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/SetInputPeriod/SetInputPeriod/) command - to set the period to read
