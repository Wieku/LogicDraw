<p align="center">
  <img alt="banner.png" src="android/assets/assets/logo/banner_inv_640.png"/>
</p>

[![Build Status](https://ci.starchasers.ovh/jenkins/buildStatus/icon?job=LogicDraw)](https://ci.starchasers.ovh/jenkins/view/Wieku/job/LogicDraw/)

LogicDraw is an application to perform simulations of logic gates, in which you can paint your circuit like in raster graphics editor.

Application is in early development, so be aware that critical bugs can occur. I don't take responsibility for lost maps and blueprints. But if such thing happens, please submit a new issue.

## Elements

#### Wires

|Element|Idle color|Active color|
|-------|----------|------------|
|Wire|![](https://placehold.it/15/7F0000/000000?text=+)|![](https://placehold.it/15/D50000/000000?text=+)|
|Dark wire|![](https://placehold.it/15/130000/000000?text=+)|![](https://placehold.it/15/200000/000000?text=+)|
|Cross|![](https://placehold.it/15/757575/000000?text=+)|![](https://placehold.it/15/9E9E9E/000000?text=+)|
|Dark cross|![](https://placehold.it/15/131313/000000?text=+)|![](https://placehold.it/15/171717/000000?text=+)|

Dark wire and cross have much smaller contrast, so they are helpful in making good-looking displays.

#### Controllers

|Element|Idle color|Active color|
|-------|----------|------------|
|Input|![](https://placehold.it/15/01579B/000000?text=+)|![](https://placehold.it/15/0277BD/000000?text=+)|
|Controller|![](https://placehold.it/15/1B5E20/000000?text=+)|![](https://placehold.it/15/2E7D32/000000?text=+)|

On non-controllable gates, Controller works like regular input.

#### Flip-flops

|Element|Idle color|Active color|
|-------|----------|------------|
|TFlipFlop|![](https://placehold.it/15/311B92/000000?text=+)|![](https://placehold.it/15/4527A0/000000?text=+)|
|Memory (DFlipFlop)|![](https://placehold.it/15/37474F/000000?text=+)|![](https://placehold.it/15/455A64/000000?text=+)|

Flip-flops react to rising-edge signal (option to change it will be added in the future).

#### Gates

|Element|Idle color|Active color|
|-------|----------|------------|
|Or gate|![](https://placehold.it/15/F57F17/000000?text=+)|![](https://placehold.it/15/F9A825/000000?text=+)|
|Nor gate|![](https://placehold.it/15/FFD600/000000?text=+)|![](https://placehold.it/15/FFEA00/000000?text=+)|
|And gate|![](https://placehold.it/15/00BFA5/000000?text=+)|![](https://placehold.it/15/1DE9B6/000000?text=+)|
|Nand gate|![](https://placehold.it/15/004D40/000000?text=+)|![](https://placehold.it/15/00695C/000000?text=+)|
|Xor gate|![](https://placehold.it/15/C51162/000000?text=+)|![](https://placehold.it/15/F50057/000000?text=+)|
|Xnor gate|![](https://placehold.it/15/880E4F/000000?text=+)|![](https://placehold.it/15/AD1457/000000?text=+)|
|Delay gate|![](https://placehold.it/15/827717/000000?text=+)|![](https://placehold.it/15/9E9D24/000000?text=+)|
|PWM generator|![](https://placehold.it/15/AA00FF/000000?text=+)|![](https://placehold.it/15/D500F9/000000?text=+)|
|Stop gate|![](https://placehold.it/15/BF360C/000000?text=+)|![](https://placehold.it/15/D84315/000000?text=+)|
|Key gate|![](https://placehold.it/15/3E2723/000000?text=+)|![](https://placehold.it/15/4E342E/000000?text=+)|
|Programmer gate|![](https://placehold.it/15/21274F/000000?text=+)|![](https://placehold.it/15/424A64/000000?text=+)|
|RAM gate|![](https://placehold.it/15/c7b365/000000?text=+)|![](https://placehold.it/15/c7b365/000000?text=+)|
|IO gate|![](https://placehold.it/15/c99ffe/000000?text=+)|![](https://placehold.it/15/c99ffe/000000?text=+)|

Stop gate is for circuit debug purposes, so it should not be generally used. It stops the world clock on rising-edge signal.

Key gate is a binding to real-world keyboard, high-level signal disables it.

Programmer gate outputs stored data bit by bit on rising-edge signal.

#### Display

|Element|Idle color|Active color|
|-------|----------|------------|
|White pixel|![](https://placehold.it/15/111111/000000?text=+)|![](https://placehold.it/15/FAFAFA/000000?text=+)|
|Red pixel|![](https://placehold.it/15/1B1010/000000?text=+)|![](https://placehold.it/15/FE2626/000000?text=+)|
|Green pixel|![](https://placehold.it/15/121512/000000?text=+)|![](https://placehold.it/15/19FE19/000000?text=+)|

White, Red and Green pixels work just like regular Wire.

#### Misc

|Element|Idle color|Active color|
|-------|----------|------------|
|Description|![](https://placehold.it/15/8D6E63/000000?text=+)|![](https://placehold.it/15/8D6E63/000000?text=+)|

##### IO Gate

IO Gate can be used to output and read to/from the console. It only has single input and 4 states:
`IDLE`, `CMD`, `READ`, `RESPOND`. Normally (when input is low) gate is IDLE. States change in the following way:

* `IDLE` && `high` -> `CMD`
* `CMD` && `low` -> `READ`
* `CMD` && `high` -> `RESPOND`

After entering `READ`, the gate will form a byte from bits read over 8 ticks in a way that the least significant
bit is read last. After all bits are read, it will output the byte to the console and back into `IDLE`.

After entering `RESPOND`, the gate will check if there is data in console buffer, output low and go into `IDLE` if there
isn't, or output high and 8 bits of the first character in console buffer, starting from the lowest bit, and then go
into `IDLE`

##### RAM Gate

It's.. complicated. Try reading https://gist.github.com/magik6k/d1a739a5f032e93aba2742b9fa243a26

## How to run it
To run the project, just type: `./gradlew desktop:run` (`gradlew desktop:run` on Windows CMD)

## Example circuits

BCD adder:

![bcdadder](https://static.wieku.me/logicdraw/images/BCDAdder.gif)

27 bit divider with 7 bit fraction:

![divider](https://static.wieku.me/logicdraw/images/27BitDivider.gif)

White 7 segment display with bcd decoders, showing result from the division above:

![display](https://static.wieku.me/logicdraw/images/DisplayWDecoders.png)

4 digit shift register (digits are typed by keyboard):

![shift](https://static.wieku.me/logicdraw/images/4DigitShift.gif)
