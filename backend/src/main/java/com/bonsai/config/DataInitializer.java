package com.bonsai.config;

import com.bonsai.entity.Bonsai;
import com.bonsai.entity.CareLog;
import com.bonsai.entity.Category;
import com.bonsai.entity.User;
import com.bonsai.repository.BonsaiRepository;
import com.bonsai.repository.CareLogRepository;
import com.bonsai.repository.CategoryRepository;
import com.bonsai.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BonsaiRepository bonsaiRepository;
    private final CareLogRepository careLogRepository;

    public DataInitializer(CategoryRepository categoryRepository,
                           UserRepository userRepository,
                           BonsaiRepository bonsaiRepository,
                           CareLogRepository careLogRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.bonsaiRepository = bonsaiRepository;
        this.careLogRepository = careLogRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            initCategories();
        }
        if (!userRepository.existsByUsername("bonsai_master")) {
            User defaultUser = initDefaultUser();
            if (bonsaiRepository.count() == 0) {
                initSampleBonsaisAndCareLogs(defaultUser);
            }
        }
    }

    private User initDefaultUser() {
        User user = new User();
        user.setUsername("bonsai_master");
        user.setNickname("盆景大师");
        user.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi");
        user.setEmail("master@bonsai.com");
        user.setBio("专注盆景造型与养护二十年，热爱分享盆景艺术。");
        user.setAvatar("");
        return userRepository.save(user);
    }

    private void initSampleBonsaisAndCareLogs(User user) {
        Category fiveNeedlePine = categoryRepository.findByNameAndType("五针松", "species").orElse(null);
        Category blackPine = categoryRepository.findByNameAndType("黑松", "species").orElse(null);

        Bonsai bonsai1 = new Bonsai();
        bonsai1.setUserId(user.getId());
        bonsai1.setName("云海苍松");
        bonsai1.setSpeciesId(fiveNeedlePine != null ? fiveNeedlePine.getId() : null);
        bonsai1.setAcquireDate(LocalDate.of(2020, 3, 15));
        bonsai1.setTreeAge(15);
        bonsai1.setPotType("紫砂圆盆");
        bonsai1.setDescription("一盆经典的五针松盆景，枝干遒劲，层次分明，宛如云海中的苍松。");
        bonsai1.setTrunkShape("直干式");
        bonsai1.setBranchSupport("三枝托布局");
        bonsai1.setCrownWidth("约45cm");
        bonsai1.setPotSurface("苔藓铺面");
        bonsai1.setViewCount(128);
        bonsai1 = bonsaiRepository.save(bonsai1);

        Bonsai bonsai2 = new Bonsai();
        bonsai2.setUserId(user.getId());
        bonsai2.setName("龙吟");
        bonsai2.setSpeciesId(blackPine != null ? blackPine.getId() : null);
        bonsai2.setAcquireDate(LocalDate.of(2019, 8, 20));
        bonsai2.setTreeAge(25);
        bonsai2.setPotType("长方形陶盆");
        bonsai2.setDescription("黑松老桩，树皮龟裂如龙鳞，气势磅礴。");
        bonsai2.setTrunkShape("斜干式");
        bonsai2.setBranchSupport("双主干造型");
        bonsai2.setViewCount(256);
        bonsai2 = bonsaiRepository.save(bonsai2);

        CareLog fertilizeLog = new CareLog();
        fertilizeLog.setUserId(user.getId());
        fertilizeLog.setBonsaiId(bonsai1.getId());
        fertilizeLog.setLogType("fertilize");
        fertilizeLog.setTitle("春季施肥");
        fertilizeLog.setContent("气温回升，开始春季施肥，促进新芽萌发。薄肥勤施，每10天一次。");
        fertilizeLog.setLogDate(LocalDate.of(2024, 3, 10));
        fertilizeLog.setFertilizer("有机复合肥");
        careLogRepository.save(fertilizeLog);

        CareLog pruneLog = new CareLog();
        pruneLog.setUserId(user.getId());
        pruneLog.setBonsaiId(bonsai1.getId());
        pruneLog.setLogType("prune");
        pruneLog.setTitle("春季整形修剪");
        pruneLog.setContent("剪除徒长枝，调整树形层次，保持造型优美。注意保留新芽点。");
        pruneLog.setLogDate(LocalDate.of(2024, 3, 25));
        pruneLog.setPosition("顶枝及侧枝");
        careLogRepository.save(pruneLog);

        CareLog repotLog = new CareLog();
        repotLog.setUserId(user.getId());
        repotLog.setBonsaiId(bonsai1.getId());
        repotLog.setLogType("repot");
        repotLog.setTitle("三年换盆");
        repotLog.setContent("春季萌芽前进行换盆，修剪老根，更换新的培养土，促进根系更新。");
        repotLog.setLogDate(LocalDate.of(2024, 2, 20));
        repotLog.setSoilType("赤玉土+腐叶土+鹿沼土（5:3:2）");
        careLogRepository.save(repotLog);

        CareLog waterLog = new CareLog();
        waterLog.setUserId(user.getId());
        waterLog.setBonsaiId(bonsai2.getId());
        waterLog.setLogType("water");
        waterLog.setTitle("日常浇水");
        waterLog.setContent("夏季高温，早晚各浇一次水，叶面喷水增加湿度。避开正午高温时段。");
        waterLog.setLogDate(LocalDate.of(2024, 6, 15));
        careLogRepository.save(waterLog);

        CareLog fertilizeLog2 = new CareLog();
        fertilizeLog2.setUserId(user.getId());
        fertilizeLog2.setBonsaiId(bonsai2.getId());
        fertilizeLog2.setLogType("fertilize");
        fertilizeLog2.setTitle("秋季追肥");
        fertilizeLog2.setContent("秋季生长旺季，增施磷钾肥，增强树势，为越冬做准备。");
        fertilizeLog2.setLogDate(LocalDate.of(2024, 9, 5));
        fertilizeLog2.setFertilizer("骨粉+磷酸二氢钾");
        careLogRepository.save(fertilizeLog2);
    }

    private void initCategories() {
        List<Category> species = Arrays.asList(
            createSpecies("五针松", "松柏类", 1, "🌲"),
            createSpecies("黑松", "松柏类", 2, "🌲"),
            createSpecies("罗汉松", "松柏类", 3, "🎄"),
            createSpecies("真柏", "松柏类", 4, "🌿"),
            createSpecies("榕树", "杂木类", 5, "🌳"),
            createSpecies("榆树", "杂木类", 6, "🌳"),
            createSpecies("枫树", "杂木类", 7, "🍁"),
            createSpecies("对接白蜡", "杂木类", 8, "🌿"),
            createSpecies("黄杨", "杂木类", 9, "🌳"),
            createSpecies("三角梅", "观花观果类", 10, "🌸")
        );

        List<Category> styles = Arrays.asList(
            createStyle("直干式", "传统造型", 1),
            createStyle("斜干式", "传统造型", 2),
            createStyle("曲干式", "传统造型", 3),
            createStyle("悬崖式", "意境造型", 4),
            createStyle("临水式", "意境造型", 5),
            createStyle("文人树", "意境造型", 6),
            createStyle("丛林式", "组合造型", 7),
            createStyle("附石式", "组合造型", 8),
            createStyle("水旱盆景", "组合造型", 9),
            createStyle("微型盆景", "规格类", 10)
        );

        categoryRepository.saveAll(species);
        categoryRepository.saveAll(styles);
    }

    private Category createSpecies(String name, String groupName, int sortOrder, String icon) {
        Category category = new Category();
        category.setName(name);
        category.setType("species");
        category.setGroupName(groupName);
        category.setSortOrder(sortOrder);
        category.setIcon(icon);
        return category;
    }

    private Category createStyle(String name, String groupName, int sortOrder) {
        Category category = new Category();
        category.setName(name);
        category.setType("style");
        category.setGroupName(groupName);
        category.setSortOrder(sortOrder);
        return category;
    }
}
