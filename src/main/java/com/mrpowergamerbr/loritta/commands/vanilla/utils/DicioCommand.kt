package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import org.jsoup.Jsoup
import java.awt.Color
import java.net.URLEncoder


class DicioCommand : AbstractCommand("dicio", listOf("dicionário", "definir"), CommandCategory.UTILS) {
	override fun getUsage(): String {
		return "palavra"
	}

	override fun getDescription(): String {
		return "Procure o significado de uma palavra no dicionário!"
	}

	override fun getExample(): List<String> {
		return listOf("sonho");
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.size == 1) {
			val palavra = URLEncoder.encode(context.args[0], "UTF-8");
			val httpRequest = HttpRequest.get("https://www.dicio.com.br/pesquisa.php?q=$palavra")
					.userAgent(Constants.USER_AGENT)
			val response = httpRequest.body();
			if (httpRequest.code() == 404) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + "Palavra não encontrada no meu dicionário!");
				return;
			}
			val jsoup = Jsoup.parse(response);

			// Se a página não possui uma descrição ou se ela possui uma descrição mas começa com "Ainda não temos o significado de", então é uma palavra inexistente!
			if (jsoup.select("p[itemprop = description]").isEmpty() || jsoup.select("p[itemprop = description]")[0].text().startsWith("Ainda não temos o significado de")) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + "Palavra não encontrada no meu dicionário!");
				return;
			}

			val description = jsoup.select("p[itemprop = description]")[0];

			val type = description.getElementsByTag("span")[0]
			val what = description.getElementsByTag("span")[1]
			val etim = if (description.getElementsByClass("etim").size > 0) description.getElementsByClass("etim").text() else "";
			var frase = if (jsoup.getElementsByClass("frase").isNotEmpty()) {
				jsoup.getElementsByClass("frase")[0]
			} else {
				null
			}

			val embed = EmbedBuilder();
			embed.setColor(Color(25, 89, 132))
			embed.setFooter(etim, null);

			embed.setTitle("📙 Significado de ${context.args[0]}")
			embed.setDescription("*${type.text()}*\n\n**${what.text()}**");

			if (jsoup.getElementsByClass("sinonimos").size > 0) {
				var sinonimos = jsoup.getElementsByClass("sinonimos")[0];

				embed.addField("🙂 Sinônimos", sinonimos.text(), false);
			}
			if (jsoup.getElementsByClass("sinonimos").size > 1) {
				var antonimos = jsoup.getElementsByClass("sinonimos")[1];

				embed.addField("🙁 Antônimos", antonimos.text(), false);
			}

			if (frase != null) {
				embed.addField("🖋 Frase", frase.text(), false);
			}

			context.sendMessage(context.getAsMention(true), embed.build());

		} else {
			this.explain(context);
		}
	}
}