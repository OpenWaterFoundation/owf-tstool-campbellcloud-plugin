# TSTool Campbell Cloud Plugin / Introduction #

*   [Introduction](#introduction)
*   [TSTool use with Campbell Cloud Web Services](#tstool-use-with-campbellcloud-web-services)

----------------------

## Introduction ##

TSTool is a powerful software tool that automates time series processing and product generation.
It was originally developed for the State of Colorado to process data for river basin modeling and has since
been enhanced to work with many data sources including:

*   United States Geological Survey (USGS) web service and file formats
*   Natural Resources Conservation Service (NRCS) web services
*   Regional Climate Center (RCC) Applied Climate Information Service (ACIS) web services
*   US Army Corps of Engineers DSS data files
*   others

TSTool is maintained by the Open Water Foundation,
which also enhances the software based on project needs.

*   See the latest [TSTool Documentation](https://opencdss.state.co.us/tstool/latest/doc-user/) to learn about core TSTool features.
*   See the [TSTool Download website](https://opencdss.state.co.us/tstool/) for the most recent software versions and documentation.
*   See the [Campbell Cloud Plugin download page](https://software.openwaterfoundation.org/tstool-campbellcloud-plugin/).

## TSTool use with Campbell Cloud Web Services ##

Campbell Cloud Web Services provide access to data that are maintained in the Campbell Cloud system.
See the following resources:

*   [Campbell Cloud Web Service Documentation](https://us-west-2.campbell-cloud.com/api/v1/docs/)

The [Campbell Cloud datastore documentation](../datastore-ref/CampbellCloud/CampbellCloud.md) describes how TSTool integrates with Campbell Cloud.

The [`ReadCampbellCloud`](../command-ref/ReadCampbellCloud/ReadCampbellCloud.md) command can be used to read time series,
in addition to [time series identifiers](../command-ref/TSID/TSID.md) that are generated from the main TSTool interface.

See the following examples showing how to query air temperature time series and the resulting time series graph.

**<p style="text-align: center;">
![tstool-main](tstool-main.png)
</p>**

**<p style="text-align: center;">
TSTool Main Interface (<a href="../tstool-main.png">see also the full-size image)</a>
</p>**

**<p style="text-align: center;">
![tstool-graph](tstool-graph.png)
</p>**

**<p style="text-align: center;">
TSTool Graph (<a href="../tstool-graph.png">see also the full-size image)</a>
</p>**
