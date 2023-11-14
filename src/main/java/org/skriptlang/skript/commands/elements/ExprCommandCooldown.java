/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package org.skriptlang.skript.commands.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.commands.api.CommandCooldown;
import org.skriptlang.skript.commands.api.ScriptCommand;
import org.skriptlang.skript.commands.api.ScriptCommandEvent;
import org.skriptlang.skript.commands.api.ScriptCommandSender;

import java.util.UUID;


@Name("Cooldown Time/Remaining Time/Elapsed Time/Last Usage/Bypass Permission")
@Description("Only usable in command events. Represents the cooldown time, the remaining time, or the elapsed time")
@Examples({
	"command /home:",
		"\tcooldown: 10 seconds",
		"\tcooldown message: You last teleported home %elapsed time% ago, you may teleport home again in %remaining time%.",
		"\ttrigger:",
			"\t\tteleport player to {home::%player%}"
})
@Since("2.2-dev33")
public class ExprCommandCooldown extends SimpleExpression<Timespan> {

	private enum CooldownType {
		REMAINING_TIME,
		ELAPSED_TIME,
		COOLDOWN_TIME
	}

	static {
		Skript.registerExpression(ExprCommandCooldown.class, Timespan.class, ExpressionType.SIMPLE,
			"[the] remaining [time] [of [the] (cooldown|wait) [(of|for) [the] [current] command]]",
			"[the] elapsed [time] [of [the] (cooldown|wait) [(of|for) [the] [current] command]]",
			"[the] ((cooldown|wait) time|[wait] time of [the] (cooldown|wait) [(of|for) [the] [current] command])");
	}

	private CooldownType pattern;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(ScriptCommandEvent.class)) {
			Skript.error("The command cooldown expression can only be used within a command");
			return false;
		}
		pattern = CooldownType.values()[matchedPattern];
		return false;
	}

	@Override
	@Nullable
	protected Timespan[] get(Event event) {
		if (!(event instanceof ScriptCommandEvent))
			return null;
		ScriptCommandEvent commandEvent = ((ScriptCommandEvent) event);
		ScriptCommand scriptCommand = commandEvent.getScriptCommand();

		ScriptCommandSender sender = commandEvent.getSender();
		CommandCooldown cooldown = scriptCommand.getCooldown();
		if (cooldown == null)
			return null;

		switch (pattern) {
			case REMAINING_TIME:
				return new Timespan[]{cooldown.getRemainingDuration(sender, event)};
			case ELAPSED_TIME:
				return new Timespan[]{cooldown.getElapsedDuration(sender, event)};
			case COOLDOWN_TIME:
				return new Timespan[]{cooldown.getDefaultCooldown()};
		}
		return null;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		switch (pattern) {
			case REMAINING_TIME:
				return "remaining time";
			case ELAPSED_TIME:
				return "elapsed time";
			case COOLDOWN_TIME:
				return "cooldown time";
		}
		assert false;
		return "unknown";
	}

}