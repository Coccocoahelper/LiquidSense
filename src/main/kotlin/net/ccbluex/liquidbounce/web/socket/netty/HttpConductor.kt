/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2023 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 *
 */
package net.ccbluex.liquidbounce.web.socket.netty

import net.ccbluex.liquidbounce.utils.client.logger
import net.ccbluex.liquidbounce.web.socket.netty.model.RequestObject
import net.ccbluex.liquidbounce.web.socket.netty.rest.RouteController

class HttpConductor {

    fun processRequestObject(requestObject: RequestObject) = runCatching {
        val context = requestObject.requestContext
        val method = context.httpMethod

        val route = RouteController.findRoute(context.path, method)
            ?: return@runCatching httpNotFound(context.path, "Route not found")
        val response = route.handler(requestObject)
        response
    }.getOrElse {
        logger.error("Error while processing request object: $requestObject", it)
        httpInternalServerError(it.message ?: "Unknown error")
    }


}
