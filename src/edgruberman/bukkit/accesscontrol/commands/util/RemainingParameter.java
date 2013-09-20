package edgruberman.bukkit.accesscontrol.commands.util;

import java.util.Collections;
import java.util.List;

public class RemainingParameter extends Parameter<List<String>> {

    public RemainingParameter(final RemainingParameter.Factory factory) {
        super(factory);
    }

    @Override
    protected List<String> parse(final ExecutionRequest request) throws ArgumentContingency {
        if (this.index >= request.getArguments().size()) return Collections.emptyList();
        return request.getArguments().subList(this.index, request.getArguments().size());
    }





    public static class Factory extends Parameter.Factory<RemainingParameter, List<String>, RemainingParameter.Factory> {

        public static RemainingParameter.Factory create(final String name) {
            return new RemainingParameter.Factory().setName(name);
        }

        @Override
        public RemainingParameter.Factory cast() {
            return this;
        }

        @Override
        public RemainingParameter build() {
            return new RemainingParameter(this);
        }

    }

}
