package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.CustomNpcEntity;
import com.jglims.plugin.quests.QuestManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.Set;
import java.util.UUID;

/**
 * Steve NPC - Quest-giving NPC for the Overworld quest line.
 * Found in villages. Only one Steve can exist in the world at a time.
 * No trades, only quests. Dialogue is in PT-BR.
 */
public class SteveNpc extends CustomNpcEntity {

    private static final TextColor STEVE_BLUE = TextColor.color(80, 140, 230);
    private static final TextColor QUEST_GOLD = TextColor.color(255, 200, 50);

    /** Track if a Steve instance already exists globally. */
    private static volatile UUID activeSteve = null;

    private NamespacedKey keyHunterStage;
    private NamespacedKey keyMinerStage;

    public SteveNpc(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.STEVE_NPC);
        this.tradeRefreshDays = 0; // No trades

        this.keyHunterStage = new NamespacedKey(plugin, "quest_hunter_stage");
        this.keyMinerStage = new NamespacedKey(plugin, "quest_miner_stage");

        dialogueLines.add(Component.text("Opa! Que bom te ver por aqui, aventureiro!", STEVE_BLUE));
        dialogueLines.add(Component.text("Eu sou o Steve, o primeiro a explorar estas terras.", NamedTextColor.WHITE));
        dialogueLines.add(Component.text("Ja lutei contra Creepers, sobrevivi ao Nether...", STEVE_BLUE));
        dialogueLines.add(Component.text("Agora preciso de alguem corajoso pra continuar minha missao.", NamedTextColor.WHITE));
        dialogueLines.add(Component.text("Aceita o desafio? Tenho recompensas incriveis!", QUEST_GOLD)
                .decorate(TextDecoration.BOLD));
        dialogueLines.add(Component.text("Clique novamente para ver suas missoes.", NamedTextColor.YELLOW)
                .decorate(TextDecoration.ITALIC));
    }

    @Override
    protected void onSpawn() {
        // Enforce only one Steve in the world
        if (activeSteve != null && !activeSteve.equals(uniqueId)) {
            // Check if the other Steve is still alive
            org.bukkit.entity.Entity existing = plugin.getServer().getEntity(activeSteve);
            if (existing != null && existing.isValid() && !existing.isDead()) {
                // Another Steve exists — remove ourselves
                if (hitboxEntity != null) {
                    hitboxEntity.remove();
                }
                alive = false;
                return;
            }
        }
        activeSteve = uniqueId;
        initializeTrades();
    }

    @Override
    protected void initializeTrades() {
        // Steve has no trades — quest only
    }

    @Override
    public void onInteract(Player player) {
        // Show dialogue on first interaction
        if (!dialogueShown.contains(player.getUniqueId()) && !dialogueLines.isEmpty()) {
            showDialogue(player);
            dialogueShown.add(player.getUniqueId());
            return;
        }

        // Quest interaction
        showQuestProgress(player);
    }

    /**
     * Shows quest progress for the Overworld quest lines (Hunter + Miner).
     * Delegates to the QuestManager for actual quest advancement.
     */
    private void showQuestProgress(Player player) {
        QuestManager qm = JGlimsPlugin.getInstance().getQuestManager();
        PersistentDataContainer pdc = player.getPersistentDataContainer();

        int hunterStage = pdc.getOrDefault(keyHunterStage, PersistentDataType.INTEGER, 0);
        int minerStage = pdc.getOrDefault(keyMinerStage, PersistentDataType.INTEGER, 0);

        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("── Steve ──", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));

        if (hunterStage >= 3 && minerStage >= 3) {
            player.sendMessage(Component.text("Voce ja completou todas as missoes do Overworld!", NamedTextColor.GREEN));
            player.sendMessage(Component.text("Parabens, aventureiro! Voce e uma lenda!", QUEST_GOLD)
                    .decorate(TextDecoration.BOLD));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1.0f, 1.0f);
            return;
        }

        // Show available quests
        if (hunterStage < 3) {
            player.sendMessage(Component.text(""));
            player.sendMessage(Component.text("[Caminho do Cacador]", TextColor.color(200, 150, 50))
                    .decorate(TextDecoration.BOLD));
            if (hunterStage == 0) {
                player.sendMessage(Component.text("  Missao: Mate 30 mobs hostis", NamedTextColor.WHITE));
            } else if (hunterStage == 1) {
                player.sendMessage(Component.text("  Missao: Mate 10 King mobs", NamedTextColor.WHITE));
            } else {
                player.sendMessage(Component.text("  Missao: Mate 3 Roaming Bosses", NamedTextColor.WHITE));
            }
            player.sendMessage(Component.text("  (Clique no NPC de quest especifico para aceitar)", NamedTextColor.GRAY)
                    .decorate(TextDecoration.ITALIC));
        }

        if (minerStage < 3) {
            player.sendMessage(Component.text(""));
            player.sendMessage(Component.text("[Profundezas do Minerador]", TextColor.color(100, 200, 255))
                    .decorate(TextDecoration.BOLD));
            if (minerStage == 0) {
                player.sendMessage(Component.text("  Missao: Mine 128 minerios de diamante", NamedTextColor.WHITE));
            } else if (minerStage == 1) {
                player.sendMessage(Component.text("  Missao: Mine 32 ancient debris", NamedTextColor.WHITE));
            } else {
                player.sendMessage(Component.text("  Missao: Mine 64 minerios de esmeralda deepslate", NamedTextColor.WHITE));
            }
            player.sendMessage(Component.text("  (Clique no NPC de quest especifico para aceitar)", NamedTextColor.GRAY)
                    .decorate(TextDecoration.ITALIC));
        }

        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("Boa sorte, aventureiro! Estarei aqui quando precisar.", STEVE_BLUE));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1.0f, 1.0f);
    }

    @Override
    protected void showDialogue(Player player) {
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("── Steve ──", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        for (Component line : dialogueLines) {
            player.sendMessage(line);
        }
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("[Clique novamente para ver missoes]", NamedTextColor.YELLOW));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1.0f, 1.0f);
    }

    /**
     * Cleanup the global tracker when this NPC is removed.
     */
    @Override
    protected void onTick() {
        super.onTick();
        if (!alive && activeSteve != null && activeSteve.equals(uniqueId)) {
            activeSteve = null;
        }
    }
}
