# TSTool / Campbell Cloud Web Services Plugin / Release Notes #

Release notes are available for the core TSTool product and plugin.
The core software and plugins are maintained separately and may be updated at different times.

*   [TSTool core product release notes](https://opencdss.state.co.us/tstool/latest/doc-user/appendix-release-notes/release-notes/)
*   [TSTool Version Compatibility](#tstool-version-compatibility)
*   [Release Note Details](#release-note-details)
*   [GitHub repository issues](https://github.com/OpenWaterFoundation/owf-tstool-campbellcloud-plugin/issues) - outstanding issues

----

## TSTool Version Compatibility ##

The following table lists TSTool and plugin software version compatibility.

**<p style="text-align: center;">
TSTool and Plugin Version Compatibility
</p>**

| **Plugin Version** | **Required TSTool Version** | **Comments** |
| -- | -- | -- |
| 1.0.0+ | >= 15.0.0 | Initial release. |

## Release Note Details ##

Release notes for specific versions are listed below, with repository issue number.

*   [Version 1.0.3](#version-103)
*   [Version 1.0.2](#version-102)
*   [Version 1.0.1](#version-101)
*   [Version 1.0.0](#version-100)

----------

## Version 1.0.3 ##

**Maintenance release to improve plugin troubleshooting.**

*   ![change](change.png) Add the plugin jar file location to ***View / Datastores*** to help with troubleshooting.

## Version 1.0.2 ##

**Maintenance release to improve error handling when no data are available for a time series.**

*   ![change](change.png) Add additional error handling for case when the Campbell Cloud API returns no datastream data for a time series.

## Version 1.0.1 ##

**Maintenance release to clean up issues for the initial production release.**

*   ![change](change.png) Update the documentation for software changes and additional Campbell Cloud information.
*   ![change](change.png) Update the [`TSID`](../command-ref/TSID/TSID.md) command:
    +   Inserting `TSID` commands also inserts a comment above the command with descriptive information.
        This requires at least TSTool 15.2.0.
*   ![change](change.png) Update the [`ReadCampbellCloud`](../command-ref/ReadCampbellCloud/ReadCampbellCloud.md) command:
    +   Add `ReadTimeSeries` and `TimeSeriesCatalogTableID` parameters to allow reading a list of time series into a table.
    +   The `InputStart` and `InputEnd` time zone now defaults to the computer's time zone.
    +   The `Timezone`, which specifies the output time zone, now defaults to `UTC`.
    +   The `Timeout` parameter has been added to control the timeout for web services requests.

## Version 1.0.0 ##

**Initial release.**

*   ![new](new.png) Enable interactive browsing of Campbell Cloud web service time series (datastreams and related data).
*   ![new](new.png) Enable [Campbell Cloud `TSID`](../command-ref/TSID/TSID.md) command support
*   ![new](new.png) Enable the [`ReadCampbellCloud`](../command-ref/ReadCampbellCloud/ReadCampbellCloud.md) plugin command
