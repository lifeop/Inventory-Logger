# InventoryLogger

A Minecraft 1.8.9 Forge mod that automatically tracks and saves your inventory snapshots, allowing you to view past inventories even after disconnects or deaths.

## Features

- **Automatic Inventory Tracking**: Takes snapshots of your inventory every 15 seconds
- **Persistent Storage**: Snapshots are saved to disk and persist across game sessions
- **View Past Inventories**: Access up to 20 snapshots from the last 5 minutes
- **Death Protection**: Automatically saves your inventory before death
- **Client-Side Only**: No server-side installation required
- **Full Item Details**: View items with enchants, lore, and all NBT data

## Requirements

- Minecraft 1.8.9
- Minecraft Forge 11.15.1.2318 or compatible
- Client-side only (no server installation needed)

## Installation

1. Download the latest `inventorylogger-1.0.jar` file
2. Place it in your `.minecraft/mods/` folder
3. Launch Minecraft with Forge 1.8.9
4. The mod will automatically start tracking your inventory

## Usage

### Opening the Snapshot List

Press **L** (default keybind) to open the inventory snapshot selection GUI.

You can change the keybind in:
- **Options** → **Controls** → **Inventory** → **View Inventory Logs**

### Viewing Snapshots

1. Press **L** to open the snapshot list
2. Select a snapshot by clicking on its timestamp button
3. View the inventory with all items, enchants, and lore
4. Hover over items to see detailed tooltips
5. Press **ESC** or click outside the GUI to close

### Snapshot Information

- Snapshots are taken every 15 seconds automatically
- Up to 20 snapshots are kept in memory (5 minutes worth)
- Snapshots are saved to disk and persist across sessions
- Old snapshots are automatically cleaned up after 7 days

## Storage Location

Snapshots are stored locally in:
```
.minecraft/inventory_snapshots/[UUID].dat
```

Each player has their own snapshot file identified by their UUID.

## Keybinds

| Key | Action |
|-----|--------|
| **L** (default) | Open Inventory Snapshot List |

## Technical Details

- **Snapshot Frequency**: Every 300 ticks (15 seconds)
- **Memory Retention**: 5 minutes (20 snapshots)
- **Disk Retention**: 7 days
- **Storage Format**: Compressed NBT files
- **Side**: Client-side only

## Troubleshooting

### No snapshots available

- Wait at least 15 seconds after joining a world for the first snapshot to be taken
- Ensure you're in a world (not the main menu)
- Check that the mod is loaded (check the mods list in the main menu)

### Keybind not working

- Check that the keybind is registered in **Options** → **Controls**
- Try changing the keybind to a different key
- Ensure no other mods are conflicting with the L key

### Snapshots not persisting

- Check that you have write permissions in your `.minecraft` folder
- Verify the `inventory_snapshots` folder exists in your `.minecraft` directory
- Check the Minecraft logs for any file I/O errors

## Compatibility

This mod is designed to be compatible with other client-side mods. It does not modify any server-side behavior and works in both single-player and multiplayer.

## License

This mod is provided as-is for personal use.

## Version

**Current Version**: 1.0

## Credits

Developed for Minecraft 1.8.9 with Minecraft Forge.

