# TransRobot

john dot innovation dot au at gmail dot com

## The Design

It is very clearly the system based on leJOS cannot be a real-time system because there is a Java virtual machine acting as intermediate layer.  But anyway, it is a funny work to write some code to run in the robot.    

## System Requirements

leJOS-EV3

http://www.lejos.org/ev3.php

"There is no association between Lego and leJOS or Sun and leJOS, or even between Lego and Sun as far as we know."

JRE: JavaSE-v1.6

http://www.oracle.com/technetwork/java/javase

Java for LEGO Mindstorms EV3

http://www.oracle.com/technetwork/java/embedded/downloads/javase/javaseemeddedev3-1982511.html


## Troubleshooting

Q: How to connect to the robot?

A: The robot can be connected via blue-tooth or WiFi.  It is more convenient to connect the robot via blue-tooth since it is not necessary to setup some WiFi hotspot to make the connection works, especially in outdoor environment.    

Q: Why the ip address of robot is 10.0.1.1?  Could it be 127.0.0.1?

A: It is the default ip address when the robot is connected via blue-tooth, and it might be changed if the configuration of robot is manually modified.  The address of 127.0.0.1 can be used as a network server ip address.  But in this situation, only the programs running in the robot can connect to this network address.  Is this what you want?    

## Legal

Copyright (c) 2015-2015, John Wong <john dot innovation dot au at gmail dot com>, all rights reserved.

Apple, the Apple logo, iPad, iPhone and iPod touch are trademarks of Apple Inc.
 
LEGO, the LEGO logo and MINDSTORMS are trademarks and/or copyrights of the LEGO Group.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 
All other trademarks, logos and copyrights are the property of their respective owners and are hereby acknowledged.
