import java.util.function.Predicate;
import java.util.function.Supplier;

public class Reaction { // one chemical rule, for example fire next to gas makes an explosion

    private final Predicate<Elements> trigger; // what sets the reaction off, for example fire
    private final Predicate<Elements> reactant; // what changes when it is set off, for example gas
    private final double chance; // how likely it is each frame, 0 to 1 (0.25 is 25%)
    private final Supplier<Elements> product; // makes a brand new element for the reactant to turn into

    public Reaction(Predicate<Elements> trigger, Predicate<Elements> reactant,
            double chance, Supplier<Elements> product) { // builds a rule from its four parts
        this.trigger = trigger; // saves each part so the rule can use it later
        this.reactant = reactant;
        this.chance = chance;
        this.product = product;
    }

    public boolean matches(Physics physics, int row, int col) { // does the cell at this row and column react this frame ?
        return reactant.test(physics.grid[row][col]) // am I the kind of cell that can react, for example gas ?
                && physics.neighbours(row, col).anyMatch(trigger) // is something that sets me off actually touching me ?
                && Math.random() < chance; // do I win the random roll this frame ?
    }

    public Elements produce() { // creates what the reacting cell becomes
        return product.get(); // a new element each time, so each one has its own timer
    }
}
