# Move Tutors
A Cobblemon-Fabric Server-Side mod that introduces Move Tutoring to the game!

## Features

### Customization

Move Tutors is highly customizable, allowing server administrators to define various tutors with different sets of moves, permissions, and GUI sizes through configuration files.

### Economy Integration

Move Tutors integrates with the Impactor economy mod, enabling players to purchase moves for their Pokémon using in-game currency. This adds an additional layer of gameplay and economy management.

### GUI-Based Move Tutoring

The mod provides a comprehensive GUI system for teaching moves to Pokémon. Players can easily navigate through available moves and confirm their purchases through a user-friendly interface.

### YAML Configuration

Define tutors and their offerings through simple YAML configuration files. Each tutor can have a unique set of moves, a custom GUI size, and specific permissions required to access them.

### Commands

- **/tutormoves reload**: Reloads the configuration files.
- **/tutormoves tutor <slot>**: Opens the move tutor GUI for the Pokémon in the specified slot.
- **/tutormoves open <specific_tutor> <slot>**: Opens a specific tutor's move GUI for the Pokémon in the specified slot.

## Economy Integration

### Setting Up Prices

Each move offered by a tutor can have an associated price. This is handled within the move tutor GUI, where players can confirm their purchases before any currency is deducted.

### Example Integration

In the tutor's GUI, each move will display its price. Players must have sufficient balance to confirm the purchase, ensuring a smooth and fair economy system.

## Command Permissions

### Admin Commands

- **/tutormoves reload**: Permission `tutormoves.reload` (default OP).

### Player Commands

- **/tutormoves tutor <slot>**: Permission `tutormoves.tutor` (default OP).
- **/tutormoves open <specific_tutor> <slot>**: Permission `tutormoves.open.<specific_tutor>` (configurable per tutor).

## Usage Example

### Teaching a Move

1. **Open the GUI**: Use `/tutormoves tutor <slot>` to open the tutor GUI for the Pokémon in the specified slot.
2. **Select a Move**: Choose a move from the list of available moves.
3. **Confirm Purchase**: Confirm the purchase in the confirmation window. If the Pokémon cannot learn the move or already knows it, the player is notified and the purchase is not processed.

### Configuring a New Tutor

1. **Create a YAML File**: Define a new YAML configuration file for your tutor, e.g., `dragonmaster.yml`.
2. **Specify Details**: Include the `name`, `permission`, `size`, and `moves` for the tutor.
3. **Reload Configuration**: Use `/tutormoves reload` to apply the new configuration.

## Compatibility

Move Tutors is designed to work seamlessly with other mods, including:

- **Impactor** and **Pebbles Economy**: For economy integration.
- **LuckPerms**: For permission management.

---

Enjoy using Move Tutors and take your Pokémon training to the next level!
