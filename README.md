# RailWorld

This is a _fork_ of the RailWorld open source software that was originally made by Steve Kollmansberger in Java. Minor revisions have been made by Rick Rutt.

The primary web site for RailWorld resides at <http://www.kolls.net/rwwiki/index.php?title=Rail_World>

The following description is from that site:

### About Rail World

Rail World is a railroad train simulation game designed to bring the features of model railroading to the desktop. Rail World allows you to:

    Use actual aerial photographs or satellite images as the basis for maps.
    Manage multiple trains as a conductor.
    Control trains with signals.
    Drive trains using throttle and brake as an engineer.
    Grapple with realistic physics, including stopping distance and collisions.
    Set switches and specific train routing.
    Load and unload cargo.
    Use a variety of programmable signals.
    Participate in challenging score-based missions. 

Rail World includes both the play module and a complete map editor. 

### License

RailWorld is open source software under the GNU General Public Licesnse, Version 2 (GPL 2).

License details are available in this repository in the file **gpl.txt**

### Track Maps

The original set of RailWorld maps are available at this web site: <http://www.kolls.net/rwwiki/index.php?title=Maps>

The following additional _table top_ model maps are available in the **data** sub-folder of this GitHub repository:

- **TableTop-Basic** A simple layout based on a hobby magazine.
- **TableTop-Extended** An extended version of that hobby magazine layout.
- **TableTop-Automotive** An imaginary supply chain for a factory for hand-crafted cars.

A pair of actual layouts based on satellite imagery are also included:

- **Plymouth** An actual track system centered on a crossing of two main lines in downtown Plymouth, Michigan.
- **Wixom** An actual track and yard system at the former Ford Motor Company final assembly plant in Wixom, Michigan. The bottom of this layout connects to the top of the **Plymouth** layout. 

### Change Log

#### Version 0.9b-RRutt

(Released via GitHub.com 03-23-2014)

- Allow circular tracks without entering an _infinite loop_ during post-load processing of signal insertion.
- When a Rail Accident occurs, do not remove the affected train(s) from the track until _after_ the Rail Accident Report dialog is dismissed.
- Enlarged the track information dialog window to increase the size of the text description block.
- Compiled for Java 7 (JRE 1.7)

The runnable Java JAR file and source code files for version **0.9b-RRutt** are available from this GitHub.com repository: <https://github.com/rrutt/RailWorld.git>

#### Prior Versions

Refer to <http://www.kolls.net/rwwiki/index.php?title=ChangeLog>

The most recent version at that site is **0.9a** released 05-02-2010.

The runnable Java JAR file and source code ZIP file for version **0.9a** are available from this web page: <http://www.kolls.net/rwwiki/index.php?title=Download>

The JAR file at that site has been compiled for Java 6 (JRE 1.6)