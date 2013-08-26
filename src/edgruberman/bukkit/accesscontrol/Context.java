package edgruberman.bukkit.accesscontrol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.OfflinePlayer;

import edgruberman.bukkit.accesscontrol.Registrar.Registration;

public interface Context {

    public Map<String, Boolean> permissions(Descriptor descriptor);



    public static class PlayerContext implements Context {

        private final OfflinePlayer state;

        public PlayerContext(final OfflinePlayer state) {
            this.state = state;
        }

        @Override
        public Map<String, Boolean> permissions(final Descriptor descriptor) {
            return descriptor.permissions(this.state);
        }

    }



    /**
     * separates command arguments using references as delimiters
     * or when no references are matched, minimum arguments per descriptor
     * in order specified in configuration
     */
    public static class CommandContext implements Context {

        private final List<ArgumentSection> sections = new ArrayList<ArgumentSection>();
        private final Map<Class<? extends Descriptor>, ArgumentSection> sectionsByType = new LinkedHashMap<Class<? extends Descriptor>, ArgumentSection>();

        /** @param arguments context arguments in order supplied */
        public CommandContext(final List<Registration> registrations, final List<String> arguments) {
            final List<Registration> unmatched = new ArrayList<Registration>(registrations);

            final List<ArgumentSection> sections = this.identify(arguments, unmatched);
            if (sections.size() > 0) {
                // populate arguments for each registration reference found
                for (int i = 0; i < sections.size(); i++) {
                    final ArgumentSection section = sections.get(i);

                    // use next section as last index, end of list if no more
                    final int to = ( i + 1 < sections.size() ?  sections.get(i + 1).index : arguments.size() );

                    section.arguments = arguments.subList(section.index + 1, to);

                    this.sections.add(section);
                    this.sectionsByType.put(section.registration.getImplementation(), section);
                }

            } else {
                // when no registration references found, assume minimum
                // arguments for each registration in configuration order
                // for as many registrations as possible
                if (sections.size() == 0) {
                    final int i = 0;
                    for (final Registration registration : unmatched) {
                        final int to = i + registration.getMinimumArguments();
                        if (to > arguments.size()) break;

                        final ArgumentSection section = new ArgumentSection(i, registration);
                        section.arguments = arguments.subList(i, to);

                        this.sections.add(section);
                        this.sectionsByType.put(section.registration.getImplementation(), section);
                    }
                }
            }
        }

        /**
         * identify reference delimiters
         * @param unmatched ordered according to configuration
         * @return ordered by reference without arguments
         */
        private List<ArgumentSection> identify(final List<String> arguments, final List<Registration> unmatched) {
            final List<ArgumentSection> result = new ArrayList<ArgumentSection>();

            // identify reference delimiters to split argument groups by
            for (int i = 0; i < arguments.size(); i++) {
                final ArgumentSection next = this.next(arguments.subList(i, arguments.size()), unmatched);
                if (next == null) break;
                result.add(next);
                i += next.registration.getMinimumArguments();
            }

            return result;
        }

        /**
         * find next argument grouping, do not assign arguments yet
         * as we first need to identify all reference delimiters
         * to allow for optional arguments being supplied
         */
        private ArgumentSection next(final List<String> remaining, final Collection<Registration> unmatched) {
            for (int i = 0; i < remaining.size(); i++) {
                final String argument = remaining.get(i).toLowerCase(Locale.ENGLISH);

                final Iterator<Registration> registrations = unmatched.iterator();
                while (registrations.hasNext()) {
                    final Registration registration = registrations.next();

                    if (registration.getMinimumArguments() == 0) {
                        registrations.remove();
                        continue;
                    }

                    if (!registration.getReference().equals(argument)) continue;

                    registrations.remove();
                    return new ArgumentSection(i, registration);
                }
            }

            return null;
        }

        public ArgumentSection getSection(final Class<? extends Descriptor> type) {
            return this.sectionsByType.get(type);
        }

        public ArgumentSection getSection(final int index) {
            return this.sections.get(index);
        }

        public int size() {
            return this.sections.size();
        }

        @Override
        public Map<String, Boolean> permissions(final Descriptor descriptor) {
            final ArgumentSection section = this.sectionsByType.get(descriptor.getClass());
            return descriptor.permissions( section != null ? section.arguments : Collections.<String>emptyList() );
        }



        public static class ArgumentSection {

            final int index;
            final Registration registration;
            List<String> arguments;

            ArgumentSection(final int index, final Registration registration) {
                this.index = index;
                this.registration = registration;
            }

            public int getIndex() {
                return this.index;
            }

            public Registration getRegistration() {
                return this.registration;
            }

            public List<String> getArguments() {
                return this.arguments;
            }

        }

    }

}
