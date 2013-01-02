package edgruberman.bukkit.accesscontrol.commands;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.text.StrTokenizer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/** transforms simple space delimited command arguments to allow for double quote delimited arguments containing spaces */
public abstract class Executor implements CommandExecutor {

    protected final StrTokenizer tokenizer = new StrTokenizer();

    protected Executor() {
        this.tokenizer.setDelimiterChar(' ');
        this.tokenizer.setQuoteChar('"');
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        return this.execute(sender, command, label, this.transform(args));
    }

    public abstract boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args);

    protected List<String> transform(final String... args) {
        this.tokenizer.reset(Executor.join(args, " "));
        return Arrays.asList(this.tokenizer.getTokenArray());
    }

    protected static String join(final List<String> args, final String delim) {
        return Executor.join(args.toArray(new String[args.size()]), delim);
    }

    protected static String join(final String[] args, final String delim) {
        if (args == null || args.length == 0) return "";

        final StringBuilder sb = new StringBuilder();
        for (final String s : args) sb.append(s + delim);
        sb.delete(sb.length() - delim.length(), sb.length());
        return sb.toString();
    }

}
