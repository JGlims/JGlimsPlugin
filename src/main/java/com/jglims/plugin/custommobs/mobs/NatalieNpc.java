package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.CustomNpcEntity;
import com.jglims.plugin.quests.QuestManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Natalie NPC - Quest-giving NPC for the Explorer and Special quest lines.
 * Found in villages. No trades, only quests. Dialogue is in PT-BR.
 */
public class NatalieNpc extends CustomNpcEntity {

    private static final TextColor EXPLORER_GREEN = TextColor.color(100, 220, 130);
    private static final TextColor SPECIAL_PURPLE = TextColor.color(180, 100, 255);

    private NamespacedKey keyExplorerStage;
    private NamespacedKey keyDragonStage;
    private NamespacedKey keyAbyssalStage;

    public NatalieNpc(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.NATALIE_NPC);
        this.tradeRefreshDays = 0; // No trades

        this.keyExplorerStage = new NamespacedKey(plugin, "quest_explorer_stage");
        this.keyDragonStage = new NamespacedKey(plugin, "quest_dragon_stage");
        this.keyAbyssalStage = new NamespacedKey(plugin, "quest_abyssal_stage");

        dialogueLines.add(Component.text("Oi! Voce deve ser novo por aqui, nao e?", EXPLORER_GREEN));
        dialogueLines.add(Component.text("Eu sou a Natalie, exploradora e aventureira!", NamedTextColor.WHITE));
        dialogueLines.add(Component.text("Ja visitei todas as dimensoes, desde o End ate o Abyss.", EXPLORER_GREEN));
        dialogueLines.add(Component.text("Tem coragem de seguir meus passos?", NamedTextColor.WHITE));
        dialogueLines.add(Component.text("Tenho missoes especiais pra quem quer ser uma lenda!", SPECIAL_PURPLE)
                .decorate(TextDecoration.BOLD));
        dialogueLines.add(Component.text("Clique novamente para ver suas missoes de exploracao.", NamedTextColor.YELLOW)
                .decorate(TextDecoration.ITALIC));
    }

    @Override
    protected void initializeTrades() {
        // Natalie has no trades — quest only
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
     * Shows quest progress for the Explorer, Dragon Slayer, and Abyssal quest lines.
     */
    private void showQuestProgress(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();

        int explorerStage = pdc.getOrDefault(keyExplorerStage, PersistentDataType.INTEGER, 0);
        int dragonStage = pdc.getOrDefault(keyDragonStage, PersistentDataType.INTEGER, 0);
        int abyssalStage = pdc.getOrDefault(keyAbyssalStage, PersistentDataType.INTEGER, 0);

        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("── Natalie ──", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));

        boolean allComplete = explorerStage >= 3 && dragonStage >= 3 && abyssalStage >= 3;
        if (allComplete) {
            player.sendMessage(Component.text("Voce completou todas as minhas missoes!", NamedTextColor.GREEN));
            player.sendMessage(Component.text("Voce e o maior explorador que ja conheci!", EXPLORER_GREEN)
                    .decorate(TextDecoration.BOLD));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1.0f, 1.0f);
            return;
        }

        // Explorer quest line
        if (explorerStage < 3) {
            player.sendMessage(Component.text(""));
            player.sendMessage(Component.text("[Jornada do Explorador]", EXPLORER_GREEN)
                    .decorate(TextDecoration.BOLD));
            String[] explorerDescs = {"Visite todas as 3 dimensoes", "Encontre 5 estruturas customizadas", "Caminhe 50.000 blocos"};
            player.sendMessage(Component.text("  Missao: " + explorerDescs[explorerStage], NamedTextColor.WHITE));
            player.sendMessage(Component.text("  Estagio: " + (explorerStage + 1) + "/3", NamedTextColor.GRAY));
        }

        // Dragon Slayer quest line
        if (dragonStage < 3) {
            player.sendMessage(Component.text(""));
            player.sendMessage(Component.text("[Cacador de Dragoes]", TextColor.color(180, 0, 220))
                    .decorate(TextDecoration.BOLD));
            String[] dragonDescs = {"Mate 100 Endermen", "Mate o Ender Dragon", "Complete um evento End Rift"};
            player.sendMessage(Component.text("  Missao: " + dragonDescs[dragonStage], NamedTextColor.WHITE));
            player.sendMessage(Component.text("  Estagio: " + (dragonStage + 1) + "/3", NamedTextColor.GRAY));
        }

        // Abyssal quest line
        if (abyssalStage < 3) {
            player.sendMessage(Component.text(""));
            player.sendMessage(Component.text("[Descida Abissal]", TextColor.color(170, 0, 0))
                    .decorate(TextDecoration.BOLD));
            String[] abyssalDescs = {"Entre na dimensao do Abyss", "Mate 50 mobs no Abyss", "Derrote o Leviatã Abissal"};
            player.sendMessage(Component.text("  Missao: " + abyssalDescs[abyssalStage], NamedTextColor.WHITE));
            player.sendMessage(Component.text("  Estagio: " + (abyssalStage + 1) + "/3", NamedTextColor.GRAY));
        }

        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("  (Clique no NPC de quest especifico para aceitar)", NamedTextColor.GRAY)
                .decorate(TextDecoration.ITALIC));
        player.sendMessage(Component.text("Vai la, aventureiro! O mundo espera por voce!", EXPLORER_GREEN));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1.0f, 1.0f);
    }

    @Override
    protected void showDialogue(Player player) {
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("── Natalie ──", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        for (Component line : dialogueLines) {
            player.sendMessage(line);
        }
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("[Clique novamente para ver missoes]", NamedTextColor.YELLOW));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1.0f, 1.2f);
    }
}
