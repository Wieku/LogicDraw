![banner](https://static.wieku.me/logicdraw/banner.png)

[![Build Status](http://wieku.me:8090/buildStatus/icon?job=LogicDraw)](http://wieku.me:8090/job/LogicDraw/)

LogicDraw is an application to perform simulations of logic gates, in which you can paint your circuit like in raster graphics editor.

Application is in early development, so be aware that critical bugs can occur.

## Elements

#### Wires

|Element|Idle color|Active color|
|-------|----------|------------|
|Wire|![](https://placehold.it/15/7F0000/000000?text=+)|![](https://placehold.it/15/D50000/000000?text=+)|
|Dark wire|![](https://placehold.it/15/130000/000000?text=+)|![](https://placehold.it/15/200000/000000?text=+)|
|Cross|![](https://placehold.it/15/757575/000000?text=+)|![](https://placehold.it/15/9E9E9E/000000?text=+)|
|Dark cross|![](https://placehold.it/15/131313/000000?text=+)|![](https://placehold.it/15/171717/000000?text=+)|

#### Controllers

|Element|Idle color|Active color|
|-------|----------|------------|
|Input|![](https://placehold.it/15/01579B/000000?text=+)|![](https://placehold.it/15/0277BD/000000?text=+)|
|Controller|![](https://placehold.it/15/1B5E20/000000?text=+)|![](https://placehold.it/15/2E7D32/000000?text=+)|

On non-controllable gates, Controller works like regular input

#### Flip-flops

|Element|Idle color|Active color|
|-------|----------|------------|
|TFlipFlop|![](https://placehold.it/15/311B92/000000?text=+)|![](https://placehold.it/15/4527A0/000000?text=+)|
|Memory (DFlipFlop)|![](https://placehold.it/15/37474F/000000?text=+)|![](https://placehold.it/15/455A64/000000?text=+)|

Flip-flops react to rising-edge signal (option to change it will be added in the future)

#### Gates

|Element|Idle color|Active color|
|-------|----------|------------|
|Or gate|![](https://placehold.it/15/F57F17/000000?text=+)|![](https://placehold.it/15/4E342E/000000?text=+)|
|Nor gate|![](https://placehold.it/15/FFD600/000000?text=+)|![](https://placehold.it/15/FFEA00/000000?text=+)|
|And gate|![](https://placehold.it/15/00BFA5/000000?text=+)|![](https://placehold.it/15/1DE9B6/000000?text=+)|
|Nand gate|![](https://placehold.it/15/004D40/000000?text=+)|![](https://placehold.it/15/00695C/000000?text=+)|
|Xor gate|![](https://placehold.it/15/C51162/000000?text=+)|![](https://placehold.it/15/F50057/000000?text=+)|
|Xnor gate|![](https://placehold.it/15/880E4F/000000?text=+)|![](https://placehold.it/15/AD1457/000000?text=+)|
|Delay gate|![](https://placehold.it/15/827717/000000?text=+)|![](https://placehold.it/15/9E9D24/000000?text=+)|
|PWM generator|![](https://placehold.it/15/AA00FF/000000?text=+)|![](https://placehold.it/15/D500F9/000000?text=+)|

#### Display

|Element|Idle color|Active color|
|-------|----------|------------|
|White pixel|![](https://placehold.it/15/111111/000000?text=+)|![](https://placehold.it/15/FAFAFA/000000?text=+)|
|Red pixel|![](https://placehold.it/15/1B1010/000000?text=+)|![](https://placehold.it/15/FE2626/000000?text=+)|
|Green pixel|![](https://placehold.it/15/121512/000000?text=+)|![](https://placehold.it/15/19FE19/000000?text=+)|

White, Red and Green pixels work just like regular Wire

#### Misc

|Element|Idle color|Active color|
|-------|----------|------------|
|Description|![](https://placehold.it/15/8D6E63/000000?text=+)|![](https://placehold.it/15/8D6E63/000000?text=+)|

## How to run it
To run the project, just type: `./gradlew desktop:run` (`gradlew desktop:run` on Windows CMD)