import java.util.function.Predicate;
import java.util.function.Supplier;

public class Reaction {

    private final Predicate<Elements> trigger;
    private final Predicate<Elements> reactant;
    private final double chance;
    private final Supplier<Elements> product;

    public Reaction(Predicate<Elements> trigger, Predicate<Elements> reactant,
            double chance, Supplier<Elements> product) {
        this.trigger = trigger;
        this.reactant = reactant;
        this.chance = chance;
        this.product = product;
    }

    public boolean matches(Physics physics, int row, int col) {
        return reactant.test(physics.grid[row][col])
                && physics.neighbours(row, col).anyMatch(trigger)
                && Math.random() < chance;
    }

    public Elements produce() {
        return product.get();
    }
}
