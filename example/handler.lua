-- Provided conduit package for performing contextual operations
local conduit = require("conduit");

test_handler = {};

-- Determine if the service is of an appropriate version to collect from
function test_handler.shouldCollectTestMetric(ctx)
    local serviceVersion = ctx["service_version"];
    if not (type(serviceVersion) == "int") then
        error("Service version is not of type 'int', got", type(serviceVersion));
    end
    ctx.shouldRun = serviceVersion > 2;
    return ctx;
end

return test_handler;