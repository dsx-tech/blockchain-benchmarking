package uk.dsxt.bb.loadgenerator;

import uk.dsxt.bb.loadgenerator.load_generator.ConstantLoadGenerator;
import uk.dsxt.bb.loadgenerator.load_generator.ExponentialLoadGenerator;
import uk.dsxt.bb.loadgenerator.load_generator.LinearDecreaseLoadGenerator;
import uk.dsxt.bb.loadgenerator.load_generator.LinearLoadGenerator;
import uk.dsxt.bb.loadgenerator.load_plan.LoadGeneratorWithDuration;
import uk.dsxt.bb.loadgenerator.load_plan.LoadPlan;
import uk.dsxt.bb.loadgenerator.load_plan.LoadPlanPerNode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class LoadPlanConfig {
    static LoadPlan getLoadPlan() {
        final LoadGeneratorWithDuration d0 = new LoadGeneratorWithDuration(new ConstantLoadGenerator(1), 0);
        final LoadGeneratorWithDuration d1 = new LoadGeneratorWithDuration(new ConstantLoadGenerator(1.5), 0);
        final LoadGeneratorWithDuration d2 = new LoadGeneratorWithDuration(new ConstantLoadGenerator(2), 0);

        final LoadGeneratorWithDuration d3_1 = new LoadGeneratorWithDuration(new LinearLoadGenerator(1, 5, 0.05), TimeUnit.MINUTES.toMillis(3));
        final LoadGeneratorWithDuration d3_2 = new LoadGeneratorWithDuration(new LinearDecreaseLoadGenerator(5, 1, -0.01), 0);

        final LoadGeneratorWithDuration d4_1 = new LoadGeneratorWithDuration(new LinearDecreaseLoadGenerator(5, 1, -0.05), TimeUnit.MINUTES.toMillis(3));
        final LoadGeneratorWithDuration d4_2 = new LoadGeneratorWithDuration(new LinearLoadGenerator(1, 5, 0.01), 0);

        final LoadGeneratorWithDuration d5_1 = new LoadGeneratorWithDuration(new ConstantLoadGenerator(0.5), TimeUnit.MINUTES.toMillis(5));
        final LoadGeneratorWithDuration d5_2 = new LoadGeneratorWithDuration(new ExponentialLoadGenerator(1.1, 3), TimeUnit.MINUTES.toMillis(3));
        final LoadGeneratorWithDuration d5_3 = new LoadGeneratorWithDuration(new ExponentialLoadGenerator(1.01, 3), TimeUnit.MINUTES.toMillis(3));
        final LoadGeneratorWithDuration d5_4 = new LoadGeneratorWithDuration(new ExponentialLoadGenerator(1.001, 3), 0);

        Map<Integer, LoadGeneratorWithDuration[]> config = new HashMap<>();
        config.put(0, new LoadGeneratorWithDuration[]{d0});
        config.put(1, new LoadGeneratorWithDuration[]{d1});
        config.put(2, new LoadGeneratorWithDuration[]{d2});
        config.put(3, new LoadGeneratorWithDuration[]{d3_1, d3_2});
        config.put(4, new LoadGeneratorWithDuration[]{d4_1, d4_2});
        config.put(5, new LoadGeneratorWithDuration[]{d5_1, d5_2, d5_3, d5_4});
        return new LoadPlanPerNode(config);
    }
}
