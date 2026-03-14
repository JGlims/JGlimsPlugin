package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.legendary.LegendaryWeapon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.time.Duration;
import java.util.*;

public class AbyssDragonBoss implements Listener {
    private static final double DRAGON_HP = 1200.0;
    private static final double DR = 0.15;
    private static final int ARENA_R = 35;
    private static final int MAX_MINIONS = 6;
    private static final long COOLDOWN_MS = 30L*60*1000;
    private final JGlimsPlugin plugin;
    private final AbyssDimensionManager dimMgr;
    private EnderDragon dragon;
    private boolean active = false;
    private long lastEnd = 0;
    private int atkTick = 0, phaseTick = 0, phaseIdx = 0;
    private Location center;
    private int arenaY;
    private BukkitRunnable loop;
    private final Random rng = new Random();
    private static final EnderDragon.Phase[] PHASES = {EnderDragon.Phase.CHARGE_PLAYER, EnderDragon.Phase.BREATH_ATTACK, EnderDragon.Phase.STRAFING, EnderDragon.Phase.LAND_ON_PORTAL, EnderDragon.Phase.CHARGE_PLAYER, EnderDragon.Phase.SEARCH_FOR_BREATH_ATTACK_TARGET};

    public AbyssDragonBoss(JGlimsPlugin plugin, AbyssDimensionManager dimensionManager) { this.plugin = plugin; this.dimMgr = dimensionManager; }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (active) return;
        Player p = event.getPlayer(); World w = p.getWorld();
        if (!w.getName().equals("world_abyss")) return;
        if (System.currentTimeMillis() - lastEnd < COOLDOWN_MS && lastEnd > 0) return;
        Location loc = p.getLocation();
        Block below = w.getBlockAt(loc.getBlockX(), loc.getBlockY()-1, loc.getBlockZ());
        if (below.getType() != Material.BEDROCK) return;
        if (Math.abs(loc.getX()) > ARENA_R && Math.abs(loc.getZ()) > ARENA_R) return;
        arenaY = findArenaY(w);
        if (arenaY < 0 || Math.abs(loc.getBlockY()-arenaY) > 5) return;
        center = new Location(w, 0.5, arenaY+1, 0.5);
        startFight(p);
    }

    private void startFight(Player trigger) {
        if (active) return; active = true;
        World w = trigger.getWorld();
        for (EnderDragon d : w.getEntitiesByClass(EnderDragon.class)) d.remove();
        for (Wither wt : w.getEntitiesByClass(Wither.class)) wt.remove();
        clearMinions(w);
        Location spawn = center.clone().add(0, 20, 0);
        dragon = w.spawn(spawn, EnderDragon.class, d -> {
            d.customName(Component.text("\u2620 Abyssal Dragon \u2620", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
            d.setCustomNameVisible(true); d.setGlowing(true);
            Objects.requireNonNull(d.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(DRAGON_HP);
            d.setHealth(DRAGON_HP); d.setPodium(center); d.setPhase(EnderDragon.Phase.CIRCLING);
        });
        Title.Times t = Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(3), Duration.ofMillis(500));
        for (Player p : w.getPlayers()) { p.showTitle(Title.title(Component.text("ABYSSAL DRAGON", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD), Component.text("Your soul will be consumed...", NamedTextColor.RED, TextDecoration.ITALIC), t)); p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 0.5f); p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 0.7f); }
        w.strikeLightningEffect(center.clone().add(5,0,5)); w.strikeLightningEffect(center.clone().add(-5,0,-5));
        new BukkitRunnable() { @Override public void run() { if (dragon != null && dragon.isValid() && active) dragon.setPhase(EnderDragon.Phase.CHARGE_PLAYER); } }.runTaskLater(plugin, 60L);
        atkTick=0; phaseTick=0; phaseIdx=0;
        loop = new BukkitRunnable() { @Override public void run() { if (!active||dragon==null||dragon.isDead()||!dragon.isValid()){cancel();return;} atkTick++; phaseTick++; doAttacks(w); doCycle(); doConfine(); doParticles(w); } };
        loop.runTaskTimer(plugin, 80L, 40L);
    }

    private void doAttacks(World w) {
        if (dragon==null||!dragon.isValid()) return;
        double hp = dragon.getHealth()/DRAGON_HP;
        List<Player> near = getPlayers(w); if (near.isEmpty()) return;
        if (atkTick%5==0) { int n = hp<0.3?4:hp<0.6?3:2; for(int i=0;i<n;i++){Player t=near.get(rng.nextInt(near.size()));Location tl=t.getLocation().add((rng.nextDouble()-0.5)*4,0,(rng.nextDouble()-0.5)*4);w.strikeLightningEffect(tl);for(Player p:near)if(p.getLocation().distance(tl)<3)p.damage(6);} }
        if (atkTick%4==0) { Location dl=dragon.getLocation();w.spawnParticle(Particle.DRAGON_BREATH,dl,80,4,2,4,0.02);for(Player p:near)if(p.getLocation().distance(dl)<10){p.damage(5);p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER,60,1));}w.playSound(dl,Sound.ENTITY_ENDER_DRAGON_GROWL,1.5f,0.7f); }
        if (atkTick%8==0) { int cur=countMinions(w); if(cur<MAX_MINIONS){int s=Math.min(2,MAX_MINIONS-cur);for(int i=0;i<s;i++){Location ml=center.clone().add((rng.nextDouble()-0.5)*20,1,(rng.nextDouble()-0.5)*20);if(i%2==0)w.spawn(ml,Enderman.class,e->{e.customName(Component.text("Void Servant",NamedTextColor.DARK_PURPLE));e.addScoreboardTag("abyss_minion");Objects.requireNonNull(e.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(40);e.setHealth(40);});else w.spawn(ml,WitherSkeleton.class,ws->{ws.customName(Component.text("Abyssal Guard",NamedTextColor.DARK_RED));ws.addScoreboardTag("abyss_minion");Objects.requireNonNull(ws.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(60);ws.setHealth(60);ws.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));});}w.playSound(center,Sound.ENTITY_ENDERMAN_TELEPORT,1.5f,0.5f);} }
        if (hp<0.2) { for(Player p:near){Vector pull=dragon.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(0.3);p.setVelocity(p.getVelocity().add(pull));p.damage(2);p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS,40,0));}w.spawnParticle(Particle.SOUL_FIRE_FLAME,dragon.getLocation(),30,3,3,3,0.05);w.spawnParticle(Particle.SOUL,dragon.getLocation(),20,4,4,4,0.02); }
    }
    private void doCycle(){if(dragon==null||!dragon.isValid())return;if(phaseTick%3!=0)return;phaseIdx=(phaseIdx+1)%PHASES.length;try{dragon.setPhase(PHASES[phaseIdx]);}catch(Exception e){dragon.setPhase(EnderDragon.Phase.CIRCLING);}}
    private void doConfine(){if(dragon==null||!dragon.isValid()||center==null)return;Location dl=dragon.getLocation();double dist=dl.distance(center);if(dist>ARENA_R+15){dragon.setPodium(center);dragon.teleport(center.clone().add(0,15,0));dragon.setPhase(EnderDragon.Phase.CIRCLING);}else if(dist>ARENA_R)dragon.setPodium(center);if(dl.getY()<arenaY-2)dragon.teleport(center.clone().add(0,10,0));if(dl.getY()>arenaY+50){dragon.setPodium(center);dragon.setPhase(EnderDragon.Phase.FLY_TO_PORTAL);}}
    private void doParticles(World w){if(center==null)return;w.spawnParticle(Particle.ASH,center,15,15,10,15,0);if(dragon!=null&&dragon.isValid())w.spawnParticle(Particle.DRAGON_BREATH,dragon.getLocation(),10,2,1,2,0.01);}

    @EventHandler
    public void onBossDamage(EntityDamageEvent event) {
        if(!active||!(event.getEntity() instanceof EnderDragon))return;
        EnderDragon d=(EnderDragon)event.getEntity();
        if(dragon==null||!d.getUniqueId().equals(dragon.getUniqueId()))return;
        event.setDamage(event.getDamage()*(1-DR));
        EntityDamageEvent.DamageCause c=event.getCause();
        if(c==EntityDamageEvent.DamageCause.SUFFOCATION||c==EntityDamageEvent.DamageCause.DROWNING||c==EntityDamageEvent.DamageCause.FALL)event.setCancelled(true);
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if(!active||!(event.getEntity() instanceof EnderDragon))return;
        EnderDragon d=(EnderDragon)event.getEntity();
        if(dragon==null||!d.getUniqueId().equals(dragon.getUniqueId()))return;
        event.getDrops().clear(); event.setDroppedExp(0);
        active=false; lastEnd=System.currentTimeMillis();
        if(loop!=null)try{loop.cancel();}catch(Exception ignored){}
        World w=d.getWorld(); clearMinions(w);
        Title.Times t=Title.Times.times(Duration.ofMillis(500),Duration.ofSeconds(5),Duration.ofMillis(1500));
        for(Player p:Bukkit.getOnlinePlayers()){p.showTitle(Title.title(Component.text("VICTORY!",NamedTextColor.GOLD,TextDecoration.BOLD),Component.text("The Abyssal Dragon has been vanquished!",NamedTextColor.YELLOW),t));p.playSound(p.getLocation(),Sound.UI_TOAST_CHALLENGE_COMPLETE,1.5f,1f);}
        dropLoot(w); createExit(w);
        new BukkitRunnable(){int c=0;@Override public void run(){if(c++>=10||center==null){cancel();return;}w.spawnParticle(Particle.FIREWORK,center.clone().add(0,5,0),100,8,8,8,0.3);w.playSound(center,Sound.ENTITY_FIREWORK_ROCKET_TWINKLE,1.5f,1f);}}.runTaskTimer(plugin,0L,20L);
        dragon=null;
    }

    private void dropLoot(World w) {
        if(center==null)return; Location l=center.clone().add(0,1,0);
        w.dropItemNaturally(l,new ItemStack(Material.NETHER_STAR,3));
        w.dropItemNaturally(l,new ItemStack(Material.EXPERIENCE_BOTTLE,64));
        w.dropItemNaturally(l,new ItemStack(Material.NETHERITE_INGOT,4));
        w.dropItemNaturally(l,new ItemStack(Material.DRAGON_EGG,1));
        w.dropItemNaturally(l,new ItemStack(Material.ELYTRA,1));
        w.dropItemNaturally(l,new ItemStack(Material.TOTEM_OF_UNDYING,2));
        w.dropItemNaturally(l,new ItemStack(Material.ENCHANTED_GOLDEN_APPLE,8));
        w.dropItemNaturally(l,new ItemStack(Material.END_CRYSTAL,4));
        try{if(plugin.getLegendaryWeaponManager()!=null){LegendaryWeapon[] all=LegendaryWeapon.values();List<LegendaryWeapon> high=new ArrayList<>();for(LegendaryWeapon lw:all){String tn=lw.getTier().name();if(tn.equals("ABYSSAL")||tn.equals("MYTHIC"))high.add(lw);}for(int i=0;i<2&&!high.isEmpty();i++){LegendaryWeapon ch=high.get(rng.nextInt(high.size()));ItemStack wp=plugin.getLegendaryWeaponManager().createWeapon(ch);if(wp!=null)w.dropItemNaturally(l,wp);}}}catch(Exception ignored){}
        try{if(plugin.getPowerUpManager()!=null){w.dropItemNaturally(l,plugin.getPowerUpManager().createHeartCrystal());w.dropItemNaturally(l,plugin.getPowerUpManager().createSoulFragment());w.dropItemNaturally(l,plugin.getPowerUpManager().createPhoenixFeather());}}catch(Exception ignored){}
        for(int i=0;i<20;i++){Location ol=l.clone().add((rng.nextDouble()-0.5)*3,1,(rng.nextDouble()-0.5)*3);w.spawn(ol,ExperienceOrb.class,o->o.setExperience(500));}
    }

    private void createExit(World w) {
        if(center==null)return;int cx=center.getBlockX(),cy=center.getBlockY(),cz=center.getBlockZ();
        for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++){w.getBlockAt(cx+x,cy-1,cz+z).setType(Material.BEDROCK);if(Math.abs(x)<=1&&Math.abs(z)<=1)w.getBlockAt(cx+x,cy,cz+z).setType(Material.END_PORTAL);}
        w.playSound(center,Sound.BLOCK_END_PORTAL_SPAWN,2f,1f);
    }

    private List<Player> getPlayers(World w){List<Player> ps=new ArrayList<>();for(Player p:w.getPlayers())if(center!=null&&p.getLocation().distance(center)<=ARENA_R+10)ps.add(p);return ps;}
    private int countMinions(World w){int c=0;for(Entity e:w.getEntities())if(e.getScoreboardTags().contains("abyss_minion")&&!e.isDead())c++;return c;}
    private void clearMinions(World w){for(Entity e:w.getEntities())if(e.getScoreboardTags().contains("abyss_minion"))e.remove();}
    private int findArenaY(World w){for(int y=80;y>0;y--)if(w.getBlockAt(0,y,0).getType()==Material.BEDROCK)return y;return -1;}
    public boolean isActive(){return active;}
}