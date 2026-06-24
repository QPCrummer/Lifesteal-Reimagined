# LifeSteal Reimagined

A NeoForge reimagining of the LifeSteal SMP.

This mod was forked from Tater Certified's Fabric Server-side [LifeSteal Mod](https://modrinth.com/mod/life-steal)

## Config

Configuration is purely managed through gamerules, here is a list of gamerules and what they do.


| Gamerule                                      | Type                | Description                                                                                        | Default                     |
|-----------------------------------------------|---------------------|----------------------------------------------------------------------------------------------------|-----------------------------|
| lifesteal:death_criteria                      | DeathCriteria       | What counts as a kill: `player_only`, `any_death`, or `any_death_drop_heart`                       | `player_only`               |
| lifesteal:enable_anti_heart_dupe              | Boolean             | Whether to prevent players from harvesting infinite hearts from weak players                       | `true`                      |
| lifesteal:revive_method                       | ReviveMethod        | How to revive players: `none`, `command`, `altar`, or `totem`                                      | `altar`                     |
| lifesteal:death_action                        | DeathAction         | Whether to `ban`, `revive` or `spectator` when they reach minimum health                           | `ban`                       |
| lifesteal:gift_method                         | GiftMethod          | How to gift hearts: `manual` or `command`                                                          | `manual`                    |
| lifesteal:steal_amount                        | Integer             | The number of hearts that should be stolen upon death                                              | `1`                         |
| lifesteal:min_player_hearts                   | Integer             | The minimum number of hearts a player can reach before being banned                                | `1`                         |
| lifesteal:max_player_hearts                   | Integer             | The maximum number of hearts a player can reach                                                    | `10`                        |
| lifesteal:withdraw_method                     | WithdrawMethod      | How to withdraw hearts: `none`, `altar`, or `command`                                              | `altar`                     |
| lifesteal:auto_revival_seconds                | Integer             | The number of seconds until a player is automatically revived. Set to 0 to disable.                | `0`                         |
| lifesteal:revival_invulnerability_seconds     | Integer             | The amount of time a player is invulnerable after being revived in seconds. Set to 0 to disable.   | `0`                         |
| lifesteal:heart_stack_size                    | Integer             | The maximum stack size of the heart item                                                           | `1`                         |
| lifesteal:heart_craft_in_crafter              | Boolean             | If a heart item can be crafted in a crafter                                                        | `false`                     |
| lifesteal:limited_heart_crafting_type         | LimitedCraftingType | How limited crafting works: `until_banned`, `forever`, `heart_based`, or `none`                    | `none`                      |
| lifesteal:limited_heart_crafting_amount       | Integer             | The value/limit of limited crafting. It changes meaning depending on the limited crafting gamerule | `0`                         |
| lifesteal:altar_animations                    | Boolean             | If animations should play when interacting with an altar                                           | `true`                      |                     |
| lifesteal:new_player_invulnerability_seconds  | Integer             | The number of seconds a player is invulnerable since they started playing                          | `0`                         |


### Dead Player Json:
Located in `config/lifesteal-deaths.json`
```json
[
  {
    "deadPlayerID": "uuid",
    "deathTime": 100
  }
]
 ```

### Commands:
- `/gift <Player> <Health>` - Gifts the specified player that amount of health if they can receive it
- `/withdraw <Hearts>` - Turns physical hearts into heart items
- `/revive <Player>` - Admin command to revive a player

### Datapack Overriding:
More info can be found on Tater Certified's [wiki page](https://github.com/Tater-Certified/LifeSteal/wiki/Guides#how-to-configure-ores-with-a-datapack).

## Check out Tater Certified's other Lifesteal-compatible mods!
Are your players combat-logging so they don't lose hearts? Use [Fair Fight](https://modrinth.com/plugin/fair-fight) to _combat_ this problem entirely! It is available on all modding and plugin platforms!