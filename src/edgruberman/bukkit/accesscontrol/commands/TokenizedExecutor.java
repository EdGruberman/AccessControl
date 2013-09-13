package edgruberman.bukkit.accesscontrol.commands;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.text.StrTokenizer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/** parses command arguments according to a {@link StrTokenizer} definition */
public abstract class TokenizedExecutor implements CommandExecutor {

    protected final StrTokenizer tokenizer = new StrTokenizer();

    /** configures tokenizer to delimit by spaces using double quotes as the quote character */
    protected TokenizedExecutor() {
        this.tokenizer.setDelimiterChar(' ');
        this.tokenizer.setQuoteChar('"');
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        return this.onCommand(sender, command, label, this.transform(args));
    }

    protected List<String> transform(final String... args) {
        this.tokenizer.reset(TokenizedExecutor.join(args, " "));
        return Arrays.asList(this.tokenizer.getTokenArray());
    }

    /**
     * same as {@link CommandExecutor#onCommand(CommandSender, Command, String, String[]) CommandExecutor.onCommand} except with transformed arguments
     *
     * @param sender Source of the command
     * @param command Command which was executed
     * @param label Alias of the command which was used
     * @param args passed command arguments split by {@link #tokenizer token} definition
     * @return true if a valid command, otherwise false
     */
    protected abstract boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args);



    protected static String join(final String[] args, final String delim) {
        if (args == null || args.length == 0) return "";

        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (sb.length() > 0) sb.append(delim);
            sb.append(args[i]);
        }

        return sb.toString();
    }

}
