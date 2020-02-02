package me.zeroeightsix.kami.module.modules.development;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module.Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.KamiTessellator;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.potion.Potion;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

@Info(name = "AutoCrystal", category = Module.Category.development)
public class AutoCrystal extends Module {
  private Setting<Boolean> autoSwitch;
  
  private Setting<Boolean> players;
  
  private Setting<Boolean> mobs;
  
  private Setting<Boolean> animals;
  
  private Setting<Boolean> place;
  
  private Setting<Boolean> slow;
  
  private Setting<Double> BreakRange;
  
  private Setting<Double> PlaceRange;
  
  private Setting<Boolean> antiWeakness;
  
  private Setting<Boolean> Walls;
  
  private Setting<Double> WallsRange;
  
  private Setting<Boolean> rotate;
  
  private Setting<Double> placereset;
  
  private Setting<Double> breakreset;
  
  private Setting<Boolean> delay;
  
  private Setting<Boolean> suicide;
  
  private Setting<Boolean> RayTrace;
  
  private Setting<Boolean> Return;
  
  private BlockPos render;
  
  private Entity renderEnt;
  
  private long systemTime;
  
  public AutoCrystal() {
    this.autoSwitch = register(Settings.b("Auto Switch"));
    this.players = register(Settings.b("Players"));
    this.mobs = register(Settings.b("Mobs", false));
    this.animals = register(Settings.b("Animals", false));
    this.place = register(Settings.b("Place", false));
    this.slow = register(Settings.b("Slow", false));
    this.BreakRange = register(Settings.d("Break Range", 6.0D));
    this.PlaceRange = register(Settings.d("Place Range", 6.0D));
    this.antiWeakness = register(Settings.b("Anti Weakness", false));
    this.Walls = register(Settings.b("Walls", false));
    this.WallsRange = register(Settings.d("Walls Range", 4.5D));
    this.rotate = register(Settings.b("Rotate", true));
    this.placereset = register(Settings.d("Placements before rotate", 1.0D));
    this.breakreset = register(Settings.d("Breaks before reset", 1.0D));
    this.delay = register(Settings.b("Delay", true));
    this.suicide = register(Settings.b("Suicide Protect", true));
    this.RayTrace = register(Settings.b("RayTrace", true));
    this.Return = register(Settings.b("Return", true));
    this.systemTime = -1L;
    this.switchCooldown = false;
    this.isAttacking = false;
    this.oldSlot = -1;
    this.packetListener = new Listener(event -> {
          Packet packet = event.getPacket();
          if (packet instanceof CPacketPlayer && isSpoofingAngles) {
            ((CPacketPlayer)packet).field_149476_e = (float)yaw;
            ((CPacketPlayer)packet).field_149473_f = (float)pitch;
          } 
      });    new java.util.function.Predicate[0];
  }
  
  private static boolean togglePitch = false;
  
  private boolean switchCooldown;
  
  private boolean isAttacking;
  
  private int oldSlot;
  
  private int newSlot;
  
  double playerDistance;
  
  private int breaks;
  
  private int placements;
  
  private boolean uniDamage;
  
  private static boolean isSpoofingAngles;
  
  private static double yaw;
  
  private static double pitch;
  
  @EventHandler
  private Listener<PacketEvent.Send> packetListener;
  
  public void onUpdate() {
    double Placements = 0.0D;
    EntityEnderCrystal crystal = mc.field_71441_e.field_72996_f.stream().filter(entity -> entity instanceof EntityEnderCrystal).map(entity -> (EntityEnderCrystal)entity).min(Comparator.comparing(c -> Float.valueOf(mc.field_71439_g.func_70032_d((Entity)c)))).orElse(null);
    if (crystal != null && mc.field_71439_g.func_70032_d((Entity)crystal) <= ((Double)this.BreakRange.getValue()).doubleValue()) {
      if (((Boolean)this.Walls.getValue()).booleanValue() && !mc.field_71439_g.func_70685_l((Entity)crystal) && mc.field_71439_g.func_70032_d((Entity)crystal) <= ((Double)this.WallsRange.getValue()).doubleValue()) {
        lookAtPacket(crystal.field_70165_t, crystal.field_70163_u, crystal.field_70161_v, (EntityPlayer)mc.field_71439_g);
        mc.field_71442_b.func_78764_a((EntityPlayer)mc.field_71439_g, (Entity)crystal);
        mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
        if (((Boolean)this.delay.getValue()).booleanValue())
          this.systemTime = System.nanoTime() / 9000000L; 
        this.breaks++;
      } else if (((Boolean)this.Walls.getValue()).booleanValue() && mc.field_71439_g.func_70685_l((Entity)crystal)) {
        lookAtPacket(crystal.field_70165_t, crystal.field_70163_u, crystal.field_70161_v, (EntityPlayer)mc.field_71439_g);
        mc.field_71442_b.func_78764_a((EntityPlayer)mc.field_71439_g, (Entity)crystal);
        mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
        if (((Boolean)this.delay.getValue()).booleanValue())
          this.systemTime = System.nanoTime() / 90000000L; 
        this.breaks++;
      } else if (!((Boolean)this.Walls.getValue()).booleanValue()) {
        lookAtPacket(crystal.field_70165_t, crystal.field_70163_u, crystal.field_70161_v, (EntityPlayer)mc.field_71439_g);
        mc.field_71442_b.func_78764_a((EntityPlayer)mc.field_71439_g, (Entity)crystal);
        mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
        if (((Boolean)this.delay.getValue()).booleanValue())
          this.systemTime = System.nanoTime() / 90000000L; 
        this.breaks++;
      } 
      if (this.breaks == ((Double)this.breakreset.getValue()).doubleValue()) {
        if (((Boolean)this.rotate.getValue()).booleanValue())
          resetRotation(); 
        this.breaks = 0;
        if (((Boolean)this.Return.getValue()).booleanValue())
          return; 
      } 
    } else {
      if (((Boolean)this.rotate.getValue()).booleanValue())
        resetRotation(); 
      if (this.oldSlot != -1) {
        (Wrapper.getPlayer()).field_71071_by.field_70461_c = this.oldSlot;
        this.oldSlot = -1;
      } 
      this.isAttacking = false;
    } 
    int crystalSlot = (mc.field_71439_g.func_184614_ca().func_77973_b() == Items.field_185158_cP) ? mc.field_71439_g.field_71071_by.field_70461_c : -1;
    if (crystalSlot == -1)
      for (int l = 0; l < 9; l++) {
        if (mc.field_71439_g.field_71071_by.func_70301_a(l).func_77973_b() == Items.field_185158_cP) {
          crystalSlot = l;
          break;
        } 
      }  
    boolean offhand = false;
    if (mc.field_71439_g.func_184592_cb().func_77973_b() == Items.field_185158_cP) {
      offhand = true;
    } else if (crystalSlot == -1) {
      return;
    } 
    List<BlockPos> blocks = findCrystalBlocks();
    List<Entity> entities = new ArrayList<>();
    if (((Boolean)this.players.getValue()).booleanValue())
      entities.addAll((Collection<? extends Entity>)mc.field_71441_e.field_73010_i.stream().filter(entityPlayer -> !Friends.isFriend(entityPlayer.func_70005_c_())).collect(Collectors.toList())); 
    entities.addAll((Collection<? extends Entity>)mc.field_71441_e.field_72996_f.stream().filter(entity -> (EntityUtil.isLiving(entity) && (EntityUtil.isPassive(entity) ? (Boolean)this.animals.getValue() : (Boolean)this.mobs.getValue()).booleanValue())).collect(Collectors.toList()));
    BlockPos q = null;
    double damage = 0.5D;
    for (Entity entity : entities) {
      if (entity == mc.field_71439_g || ((EntityLivingBase)entity).func_110143_aJ() <= 0.0F)
        continue; 
      for (BlockPos blockPos : blocks) {
        double b = entity.func_174818_b(blockPos);
        this.playerDistance = b;
        if (b >= 169.0D)
          continue; 
        double d = calculateDamage(blockPos.field_177962_a + 0.5D, (blockPos.field_177960_b + 1), blockPos.field_177961_c + 0.5D, entity);
        if (d >= 12.0D) {
          this.uniDamage = true;
        } else {
          this.uniDamage = false;
        } 
        if (d > damage) {
          double self = calculateDamage(blockPos.field_177962_a + 0.5D, (blockPos.field_177960_b + 1), blockPos.field_177961_c + 0.5D, (Entity)mc.field_71439_g);
          if ((self > d && d >= ((EntityLivingBase)entity).func_110143_aJ()) || (self - 0.5D > mc.field_71439_g.func_110143_aJ() && ((Boolean)this.suicide.getValue()).booleanValue()))
            continue; 
          damage = d;
          q = blockPos;
          this.renderEnt = entity;
        } 
      } 
    } 
    if (damage == 0.5D) {
      this.render = null;
      this.renderEnt = null;
      if (((Boolean)this.rotate.getValue()).booleanValue())
        resetRotation(); 
      return;
    } 
    this.render = q;
    if (((Boolean)this.place.getValue()).booleanValue()) {
      EnumFacing f;
      if (!offhand && mc.field_71439_g.field_71071_by.field_70461_c != crystalSlot) {
        if (((Boolean)this.autoSwitch.getValue()).booleanValue()) {
          mc.field_71439_g.field_71071_by.field_70461_c = crystalSlot;
          if (((Boolean)this.rotate.getValue()).booleanValue())
            resetRotation(); 
          this.switchCooldown = true;
        } 
        return;
      } 
      lookAtPacket(q.field_177962_a + 0.5D, q.field_177960_b - 0.5D, q.field_177961_c + 0.5D, (EntityPlayer)mc.field_71439_g);
      if (((Boolean)this.RayTrace.getValue()).booleanValue()) {
        RayTraceResult result = mc.field_71441_e.func_72933_a(new Vec3d(mc.field_71439_g.field_70165_t, mc.field_71439_g.field_70163_u + mc.field_71439_g.func_70047_e(), mc.field_71439_g.field_70161_v), new Vec3d(q.field_177962_a + 0.5D, q.field_177960_b - 0.5D, q.field_177961_c + 0.5D));
        if (result == null || result.field_178784_b == null) {
          f = EnumFacing.UP;
        } else {
          f = result.field_178784_b;
        } 
        if (this.switchCooldown) {
          this.switchCooldown = false;
          return;
        } 
      } else {
        f = EnumFacing.UP;
      } 
      mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayerTryUseItemOnBlock(q, f, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0F, 0.0F, 0.0F));
      this.placements++;
      if (this.placements == ((Double)this.placereset.getValue()).doubleValue())
        resetRotation(); 
      if (isSpoofingAngles)
        if (togglePitch) {
          mc.field_71439_g.field_70125_A = (float)(mc.field_71439_g.field_70125_A + 4.0E-4D);
          togglePitch = false;
        } else {
          mc.field_71439_g.field_70125_A = (float)(mc.field_71439_g.field_70125_A - 4.0E-4D);
          togglePitch = true;
        }  
    } 
  }
  
  public void onWorldRender(RenderEvent event) {
    if (this.render != null)
      if (this.uniDamage) {
        KamiTessellator.prepare(7);
        KamiTessellator.drawBox(this.render, -1718026087, 63);
        KamiTessellator.release();
        if (this.renderEnt != null)
          Vec3d vec3d = EntityUtil.getInterpolatedRenderPos(this.renderEnt, mc.func_184121_ak()); 
      } else {
        KamiTessellator.prepare(7);
        KamiTessellator.drawBox(this.render, 1352204441, 63);
        KamiTessellator.release();
        if (this.renderEnt != null)
          Vec3d vec3d = EntityUtil.getInterpolatedRenderPos(this.renderEnt, mc.func_184121_ak()); 
      }  
  }
  
  private void lookAtPacket(double px, double py, double pz, EntityPlayer me) {
    double[] v = EntityUtil.calculateLookAt(px, py, pz, me);
    setYawAndPitch((float)v[0], (float)v[1]);
  }
  
  private boolean canPlaceCrystal(BlockPos blockPos) {
    BlockPos boost = blockPos.func_177982_a(0, 1, 0);
    BlockPos boost2 = blockPos.func_177982_a(0, 2, 0);
    if ((mc.field_71441_e.func_180495_p(blockPos).func_177230_c() != Blocks.field_150357_h && mc.field_71441_e.func_180495_p(blockPos).func_177230_c() != Blocks.field_150343_Z) || mc.field_71441_e.func_180495_p(boost).func_177230_c() != Blocks.field_150350_a || mc.field_71441_e.func_180495_p(boost2).func_177230_c() != Blocks.field_150350_a || !mc.field_71441_e.func_72872_a(Entity.class, new AxisAlignedBB(boost)).isEmpty())
      return false; 
    return true;
  }
  
  public static BlockPos getPlayerPos() {
    return new BlockPos(Math.floor(mc.field_71439_g.field_70165_t), Math.floor(mc.field_71439_g.field_70163_u), Math.floor(mc.field_71439_g.field_70161_v));
  }
  
  private List<BlockPos> findCrystalBlocks() {
    NonNullList<BlockPos> positions = NonNullList.func_191196_a();
    positions.addAll((Collection)getSphere(getPlayerPos(), ((Double)this.PlaceRange.getValue()).floatValue(), ((Double)this.PlaceRange.getValue()).intValue(), false, true, 0).stream().filter(this::canPlaceCrystal).collect(Collectors.toList()));
    return (List<BlockPos>)positions;
  }
  
  public List<BlockPos> getSphere(BlockPos loc, float r, int h, boolean hollow, boolean sphere, int plus_y) {
    List<BlockPos> circleblocks = new ArrayList<>();
    int cx = loc.func_177958_n();
    int cy = loc.func_177956_o();
    int cz = loc.func_177952_p();
    for (int x = cx - (int)r; x <= cx + r; x++) {
      for (int z = cz - (int)r; z <= cz + r; ) {
        int y = sphere ? (cy - (int)r) : cy;
        for (;; z++) {
          if (y < (sphere ? (cy + r) : (cy + h))) {
            double dist = ((cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? ((cy - y) * (cy - y)) : 0));
            if (dist < (r * r) && (!hollow || dist >= ((r - 1.0F) * (r - 1.0F)))) {
              BlockPos l = new BlockPos(x, y + plus_y, z);
              circleblocks.add(l);
            } 
            y++;
            continue;
          } 
        } 
      } 
    } 
    return circleblocks;
  }
  
  public static float calculateDamage(double posX, double posY, double posZ, Entity entity) {
    float doubleExplosionSize = 12.0F;
    double distancedsize = entity.func_70011_f(posX, posY, posZ) / doubleExplosionSize;
    Vec3d vec3d = new Vec3d(posX, posY, posZ);
    double blockDensity = entity.field_70170_p.func_72842_a(vec3d, entity.func_174813_aQ());
    double v = (1.0D - distancedsize) * blockDensity;
    float damage = (int)((v * v + v) / 2.0D * 12.0D * doubleExplosionSize + 1.0D);
    double finald = 1.0D;
    if (entity instanceof EntityLivingBase)
      finald = getBlastReduction((EntityLivingBase)entity, getDamageMultiplied(damage), new Explosion((World)mc.field_71441_e, null, posX, posY, posZ, 6.0F, false, true)); 
    return (float)finald;
  }
  
  public static float getBlastReduction(EntityLivingBase entity, float damage, Explosion explosion) {
    if (entity instanceof EntityPlayer) {
      EntityPlayer ep = (EntityPlayer)entity;
      DamageSource ds = DamageSource.func_94539_a(explosion);
      damage = CombatRules.func_189427_a(damage, ep.func_70658_aO(), (float)ep.func_110148_a(SharedMonsterAttributes.field_189429_h).func_111126_e());
      int k = EnchantmentHelper.func_77508_a(ep.func_184193_aE(), ds);
      float f = MathHelper.func_76131_a(k, 0.0F, 20.0F);
      damage *= 1.0F - f / 25.0F;
      if (entity.func_70644_a(Potion.func_188412_a(11)))
        damage -= damage / 4.0F; 
      damage = Math.max(damage - ep.func_110139_bj(), 0.0F);
      return damage;
    } 
    damage = CombatRules.func_189427_a(damage, entity.func_70658_aO(), (float)entity.func_110148_a(SharedMonsterAttributes.field_189429_h).func_111126_e());
    return damage;
  }
  
  private static float getDamageMultiplied(float damage) {
    int diff = mc.field_71441_e.func_175659_aa().func_151525_a();
    return damage * ((diff == 0) ? 0.0F : ((diff == 2) ? 1.0F : ((diff == 1) ? 0.5F : 1.5F)));
  }
  
  public static float calculateDamage(EntityEnderCrystal crystal, Entity entity) {
    return calculateDamage(crystal.field_70165_t, crystal.field_70163_u, crystal.field_70161_v, entity);
  }
  
  private static void setYawAndPitch(float yaw1, float pitch1) {
    yaw = yaw1;
    pitch = pitch1;
    isSpoofingAngles = true;
  }
  
  private static void resetRotation() {
    if (isSpoofingAngles) {
      yaw = mc.field_71439_g.field_70177_z;
      pitch = mc.field_71439_g.field_70125_A;
      isSpoofingAngles = false;
    } 
  }
  
  public void onDisable() {
    this.render = null;
    this.renderEnt = null;
    if (((Boolean)this.rotate.getValue()).booleanValue())
      resetRotation(); 
  }
}
