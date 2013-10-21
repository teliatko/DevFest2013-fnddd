DevFest2013-fnddd
=================

Example code from the talk about "Functional Domain Modeling" on [DevFest 2013 Vienna](http://www.devfest.at/). 
[Here](https://speakerdeck.com/teliatko/functional-domain-modeling) you can find slides of the talk.

#Content
Talk discusses how to apply certain basic functional programming techniques on domain model. I showed on example how to convert a typical 
_javaesque_ code to immutable one with function compostion of invariants. In the end of the talk I mentioned suitable architecture combining CQRS, Event Sourcing and Actors.
Example uses Scala.

#References
The talk was inspired from following sources:

1. https://github.com/dwestheide/eventhub-flatmap2013
2. https://github.com/debasishg/cqrs-akka
3. https://github.com/erikrozendaal/immutable-domain-example

All of them contains complete running examples of above mentioned concepts and sketched architecture.
I inculded only references related to code examples, all other refernces can be found at the end of the talk slides.
