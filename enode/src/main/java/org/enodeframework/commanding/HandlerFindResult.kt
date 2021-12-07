package org.enodeframework.commanding

class HandlerFindResult(var findStatus: HandlerFindStatus, var findHandler: CommandHandlerProxy?) {

    constructor(findStatus: HandlerFindStatus) : this(findStatus, null)

    companion object {
        var NotFound: HandlerFindResult = HandlerFindResult(HandlerFindStatus.NotFound)
        var TooManyHandlerData: HandlerFindResult = HandlerFindResult(HandlerFindStatus.TooManyHandlerData)
        var TooManyHandler: HandlerFindResult = HandlerFindResult(HandlerFindStatus.TooManyHandler)
    }

}