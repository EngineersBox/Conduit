-- Provided conduit package for performing contextual operations
--local conduit = require("conduit");

test_handler = {};

test_handler.other = "value";

-- Determine if the service is of an appropriate version to collect from
function test_handler.shouldCollectTestMetric(self, ctx)
    for k,v in pairs(ctx) do
        print("Key: "..k.." Value: "..type(v));
    end
    local serviceVersion = ctx["service_version"];
    print(serviceVersion);
    local serviceVersionType = type(serviceVersion);
    if not (serviceVersionType == "number") then
        error("Service version is not of type 'number', got " .. serviceVersionType, 1);
    end
    ctx.executionContext.shouldRun = serviceVersion > 2;
    return ctx;
end

return test_handler;