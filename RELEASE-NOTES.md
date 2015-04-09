Release Notes
=====================

0.3.0 (in progress)
-------------------
- using copy of data internally when creating streams to avoid ConcurrentModificationException if underlying set is changed during stream handling

0.2.0
-----
- changed package names to match Maven group id
- using copied data set to produce JSON data to avoid ConcurrentModificationException (happens while saving data to file and changing store data simultaneously)
- added slf4j logging api
- using UTF-8 for file operations

0.1.0
-----
- initial release