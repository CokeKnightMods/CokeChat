# CokeChat 1.3.0 — Third update

Implemented from the shared “CokeChat – 3rd Update” requirements.

## Rendering and reading position

The background is drawn as one panel per chat render, independently from line fades and offsets. Opening reveals this panel from its lower-left anchor; message wrapping, configured dimensions and final position do not change during the animation. Four easing curves, preset/custom percentages and milliseconds, and separate delayed/staggered message fades are configurable under Chat Animation.

Incoming messages capture an identity-based viewport anchor before insertion and restore its line offset afterward. The same rule applies to normal chat and separately wrapped Peek chat, including compact counters and bursts. Only a viewport already at the bottom follows incoming content. Smooth scrolling keeps its interpolation when shifted to preserve the anchor. An idle Peek at the bottom is rebuilt lazily rather than rewrapping the entire history for every incoming message.

## Input and colors

Chat Input exposes screen/relative X and Y, width, height, font size, background opacity and rounding, and independent background/border/text colors. Text scaling also adjusts mouse position mapping and logical text width. Vanilla command entry, drafts and sending remain in place. Emoji autocomplete follows the relocated input and stays on screen.

Each color field creates its own picker instance, target callbacks, original snapshot and HSV/RGBA draft. The wheel is uploaded once as a local texture and released on close. Wheel, brightness, alpha, HEX and RGB changes preview immediately. Apply retains changes; Cancel/Escape restores only that field's original RGB/alpha. HEX accepts both six- and eight-digit forms, including typing alpha after the six-digit color.

The preview uses only local Clashbad/CokeKnight samples. Replay restarts opening, New message simulates an arrival, Duplicate updates the latest group. Input geometry and colors are displayed, and preview history has the same bottom-follow/reading distinction.

## Validation

- 34 JUnit tests pass, including opening distance/duration/easing endpoints, delay/stagger, group/wrapped-line anchors, interpolation preservation, input bounds/position modes, RGBA independence and old-config compatibility.
- Fabric client game test passes against actual Minecraft 26.1.2 with Java 25. It covers the existing full emoji catalog, standard colors, history, filters, unchanged outgoing input, action hitboxes and Peek; new checks exercise bursts, compact groups, independent Peek reading, return-to-bottom, relocated/scaled input, independent picker targets, live RGBA typing, Cancel and Apply.
- Test screenshots were inspected for wheel geometry, preview layout, panel continuity, opening and scaled input. Release evidence is under `verification/1.3.0/`.
- Static network audit passes for 193 compiled classes, including nested Fabric modules. This is not OS packet capture. No CokeChat networking references were found.
- Third-party mod interaction during a live authenticated Hypixel session was not tested.

JAR SHA-256:

`842E8A01F3F6831B26BC435E3B7B06FC2310C55AF6DF77E460C35C80F5FDDBDE`

Existing settings are retained; Gson initializes newly added options with defaults, followed by range validation. No changes to the user's current Compact Chat time window or custom Visual Words are required.
