package com.mrpowergamerbr.loritta.frontend.views.subviews.configure

import com.mrpowergamerbr.loritta.frontend.evaluate
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import net.dv8tion.jda.core.entities.Guild
import org.jooby.Request
import org.jooby.Response

class ConfigureAutoroleView : ConfigureView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		super.handleRender(req, res, variables)
		return req.path().matches(Regex("^/dashboard/configure/[0-9]+/autorole"))
	}

	override fun renderConfiguration(req: Request, res: Response, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth, guild: Guild, serverConfig: ServerConfig): String {
		variables["saveType"] = "autorole"
		serverConfig.autoroleConfig.roles = serverConfig.autoroleConfig.roles.filter {
			try {
				guild.getRoleById(it) != null
			} catch (e: Exception) {
				false
			}
		}.toMutableList()
		variables["currentAutoroles"] = serverConfig.autoroleConfig.roles.joinToString(separator = ";")
		return evaluate("autorole.html", variables)
	}
}