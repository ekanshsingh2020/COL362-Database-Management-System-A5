package rules;

import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.logical.LogicalFilter;
import org.apache.calcite.rel.logical.LogicalProject;
import org.apache.calcite.rel.logical.LogicalTableScan;
import org.apache.calcite.rel.type.RelDataType;

import convention.PConvention;
import rel.PTableScan;
import rel.PProjectFilter;

import org.checkerframework.checker.nullness.qual.Nullable;


public class PRules {

    private PRules(){
    }

    public static final RelOptRule P_TABLESCAN_RULE = new PTableScanRule(PTableScanRule.DEFAULT_CONFIG);

    private static class PTableScanRule extends ConverterRule {

        public static final Config DEFAULT_CONFIG = Config.INSTANCE
                .withConversion(LogicalTableScan.class,
                        Convention.NONE, PConvention.INSTANCE,
                        "PTableScanRule")
                .withRuleFactory(PTableScanRule::new);

        protected PTableScanRule(Config config) {
            super(config);
        }

        @Override
        public @Nullable RelNode convert(RelNode relNode) {

            TableScan scan = (TableScan) relNode;
            final RelOptTable relOptTable = scan.getTable();

            if(relOptTable.getRowType() == scan.getRowType()) {
                return PTableScan.create(scan.getCluster(), relOptTable);
            }

            return null;
        }
    }

    // Write a class PProjectFilterRule that converts a LogicalProject followed by a LogicalFilter to a single PProjectFilter node.
    
    // You can make any changes starting here.
    public static class PProjectFilterRule extends RelOptRule{

        public static final PProjectFilterRule INSTANCE = new PProjectFilterRule();

        private PProjectFilterRule() {
            // project is above the filter
            super(operand(LogicalProject.class, operand(LogicalFilter.class, any())), "PProjectFilterRule");
        }
    
        @Override
        public void onMatch(RelOptRuleCall call) {
            final LogicalProject project = call.rel(0);
            final LogicalFilter filter = call.rel(1);
            
            // Create PProjectFilter node by combining the Project and Filter nodes
            final RelNode input = filter.getInput();
            final RelNode pProjectFilter = new PProjectFilter(
                project.getCluster(),
                project.getTraitSet().replace(PConvention.INSTANCE),
                input,
                project.getProjects(),
                project.getRowType(),
                filter.getCondition()
            );
    
            call.transformTo(pProjectFilter);
        }

    

    }

}
