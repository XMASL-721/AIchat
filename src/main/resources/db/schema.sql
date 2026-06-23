-- ============================================================
-- 智能问答助手 - 数据库建表脚本
-- 数据库: aichat (先执行 CREATE DATABASE aichat DEFAULT CHARSET utf8mb4;)
-- ============================================================

CREATE DATABASE IF NOT EXISTS aichat DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE aichat;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`             BIGINT        PRIMARY KEY AUTO_INCREMENT,
    `phone`          VARCHAR(20)   NOT NULL UNIQUE COMMENT '手机号',
    `password`       VARCHAR(100)  NOT NULL       COMMENT 'BCrypt 加密密码',
    `nickname`       VARCHAR(50)   DEFAULT '用户' COMMENT '昵称',
    `avatar`         LONGTEXT      DEFAULT NULL   COMMENT '头像 Base64',
    `gender`         TINYINT       DEFAULT NULL   COMMENT '性别: 0女 1男',
    `birthday`       DATE          DEFAULT NULL   COMMENT '出生日期',
    `height`         DECIMAL(5,1)  DEFAULT NULL   COMMENT '身高(cm)',
    `weight`         DECIMAL(5,1)  DEFAULT NULL   COMMENT '体重(kg)',
    `target_weight`  DECIMAL(5,1)  DEFAULT NULL   COMMENT '目标体重(kg)',
    `role`           VARCHAR(20)   DEFAULT 'user' COMMENT '角色: user/admin',
    `created_at`     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 会话表
CREATE TABLE IF NOT EXISTS `conversation` (
    `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `user_id`     BIGINT       NOT NULL       COMMENT '所属用户 ID',
    `title`       VARCHAR(100) DEFAULT '新对话' COMMENT '会话标题',
    `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_updated_at` (`updated_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- 消息表
CREATE TABLE IF NOT EXISTS `message` (
    `id`              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `conversation_id` BIGINT       NOT NULL       COMMENT '所属会话 ID',
    `role`            VARCHAR(20)  NOT NULL       COMMENT '角色: user / assistant',
    `content`         TEXT         NOT NULL       COMMENT '消息内容',
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`conversation_id`) REFERENCES `conversation`(`id`) ON DELETE CASCADE,
    INDEX `idx_conversation_id` (`conversation_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 减脂餐表
CREATE TABLE IF NOT EXISTS `meal` (
    `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `name`        VARCHAR(100) NOT NULL       COMMENT '餐品名称',
    `ingredients` TEXT         NOT NULL       COMMENT '食材清单',
    `steps`       TEXT         NOT NULL       COMMENT '做法步骤',
    `image_url`   VARCHAR(255) DEFAULT NULL   COMMENT '图片 URL',
    `calories`    INT          DEFAULT NULL   COMMENT '卡路里(千卡)',
    `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='减脂餐表';

-- 插入示例减脂餐数据
INSERT INTO `meal` (`name`, `ingredients`, `steps`, `calories`) VALUES
('香煎鸡胸肉沙拉', '鸡胸肉 200g、生菜 100g、圣女果 8颗、黄瓜 1根、橄榄油 1勺、黑胡椒、盐、柠檬汁', '1. 鸡胸肉从中间片开，用盐、黑胡椒、柠檬汁腌制15分钟\n2. 生菜洗净撕碎，圣女果对半切，黄瓜切片\n3. 平底锅加橄榄油，中火煎鸡胸肉每面5分钟至金黄\n4. 鸡胸肉切片铺在沙拉上，淋上少许橄榄油即可', 280),
('藜麦蔬菜碗', '藜麦 100g、西兰花 100g、胡萝卜 1根、玉米粒 50g、鸡蛋 1个、生抽 1勺、香油', '1. 藜麦淘洗后加水煮15分钟，沥干备用\n2. 西兰花切小朵焯水，胡萝卜切丁焯水\n3. 鸡蛋煮熟切瓣\n4. 所有食材放入碗中，淋入生抽和香油拌匀即可', 350),
('清蒸三文鱼', '三文鱼 200g、姜片 5片、葱丝、蒸鱼豉油 2勺、料酒 1勺、橄榄油', '1. 三文鱼洗净抹干，两面抹少许盐和料酒\n2. 盘底铺姜片，放上三文鱼\n3. 水开后上锅蒸8分钟\n4. 倒掉盘中蒸出的汁水，撒上葱丝\n5. 淋上蒸鱼豉油，浇一勺热油即可', 320),
('番茄鸡蛋豆腐汤', '番茄 2个、嫩豆腐 200g、鸡蛋 2个、葱花、盐、白胡椒粉', '1. 番茄切块，豆腐切小块，鸡蛋打散\n2. 锅中少油炒番茄至出汁\n3. 加入适量清水烧开，放入豆腐煮3分钟\n4. 淋入蛋液搅散，加盐和白胡椒粉调味\n5. 撒葱花出锅', 180),
('虾仁西兰花炒糙米饭', '虾仁 150g、西兰花 100g、糙米饭 200g、蒜末、盐、黑胡椒、生抽', '1. 虾仁用少许盐和料酒腌制5分钟\n2. 西兰花焯水切小朵\n3. 锅中少油炒香蒜末，加入虾仁炒至变色\n4. 加入糙米饭和西兰花翻炒\n5. 加生抽、盐、黑胡椒调味即可', 420),
('牛油果鸡蛋三明治', '全麦面包 2片、牛油果 半个、鸡蛋 1个、生菜叶、黑胡椒、盐', '1. 鸡蛋煮熟切片，牛油果挖出压成泥\n2. 全麦面包烤至微脆\n3. 面包上抹牛油果泥，铺上生菜叶和鸡蛋片\n4. 撒黑胡椒和盐，盖上另一片面包即可', 290);

-- 健身训练表
CREATE TABLE IF NOT EXISTS `workout` (
    `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `name`        VARCHAR(100) NOT NULL       COMMENT '训练名称',
    `category`    VARCHAR(50)  NOT NULL       COMMENT '类别：腹肌/胸肌/背部/腿部/手臂/全身/有氧',
    `exercises`   TEXT         NOT NULL       COMMENT '训练动作清单',
    `duration`    INT          NOT NULL       COMMENT '训练时长(分钟)',
    `calories`    INT          DEFAULT NULL   COMMENT '预估消耗卡路里',
    `level`       VARCHAR(20)  DEFAULT '初级' COMMENT '难度：初级/中级/高级',
    `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健身训练表';

-- 插入示例健身训练数据
INSERT INTO `workout` (`name`, `category`, `exercises`, `duration`, `calories`, `level`) VALUES
('腹肌撕裂者', '腹肌', '1. 仰卧卷腹 20次×3组\n2. 平板支撑 60秒×3组\n3. 俄罗斯转体 30次×3组\n4. 仰卧举腿 15次×3组\n5. 侧平板支撑 左右各30秒×2组', 30, 200, '中级'),
('七分钟燃脂HIIT', '全身', '1. 开合跳 30秒\n2. 深蹲 30秒\n3. 高抬腿 30秒\n4. 波比跳 30秒\n5. 登山跑 30秒\n6. 原地踏步 30秒', 7, 80, '初级'),
('胸部塑形训练', '胸肌', '1. 标准俯卧撑 15次×4组\n2. 宽距俯卧撑 12次×3组\n3. 钻石俯卧撑 10次×3组\n4. 平板哑铃飞鸟 12次×4组\n5. 上斜俯卧撑 15次×3组', 40, 250, '中级'),
('美背雕刻训练', '背部', '1. 俯身哑铃划船 12次×4组\n2. 超人式 15次×4组\n3. 弹力带高位下拉 15次×3组\n4. 鸟狗式 20次×3组\n5. 眼镜蛇式拉伸 30秒×3组', 35, 200, '中级'),
('蜜桃臀腿部训练', '腿部', '1. 深蹲 20次×4组\n2. 弓步蹲 左右各15次×3组\n3. 臀桥 20次×4组\n4. 相扑深蹲 15次×3组\n5. 侧卧抬腿 左右各20次×3组', 40, 300, '中级'),
('晨间唤醒瑜伽', '全身', '1. 猫牛式 10次呼吸\n2. 下犬式 保持30秒\n3. 战士一式 左右各30秒\n4. 树式 左右各20秒\n5. 婴儿式 保持1分钟', 15, 60, '初级'),
('沙袋燃脂搏击', '有氧', '1. 直拳组合 1分钟×3组\n2. 勾拳组合 1分钟×3组\n3. 膝撞 30秒×3组\n4. 侧踢 左右各15次×3组\n5. 空击组合 2分钟×2组', 30, 350, '高级'),
('麒麟臂打造', '手臂', '1. 哑铃弯举 15次×4组\n2. 锤式弯举 12次×3组\n3. 窄距俯卧撑 12次×3组\n4. 哑铃臂屈伸 15次×3组\n5. 集中弯举 左右各12次×3组', 35, 200, '中级'),
('腹肌马甲线进阶', '腹肌', '1. 悬垂举腿 12次×3组\n2. 卷腹自行车 30次×3组\n3. 腹肌轮 12次×3组\n4. V字卷腹 15次×3组\n5. 反向卷腹 20次×3组', 35, 250, '高级'),
('跑步机高效燃脂', '有氧', '1. 慢跑热身 5分钟\n2. 间歇冲刺 30秒快+30秒慢×10组\n3. 匀速跑 10分钟\n4. 爬坡走 5分钟\n5. 慢走整理 5分钟', 35, 400, '初级'),
('弹力带全身塑形', '全身', '1. 弹力带深蹲 15次×3组\n2. 弹力带划船 15次×3组\n3. 弹力带侧平举 12次×3组\n4. 弹力带臀桥 20次×3组\n5. 弹力带拉伸 30秒×3组', 25, 150, '初级'),
('核心稳定性训练', '腹肌', '1. 死虫式 20次×3组\n2. 鸟狗式进阶 15次×3组\n3. 瑞士球平板支撑 45秒×3组\n4. 药球转体 20次×3组\n5. 单腿臀桥 左右各15次×3组', 30, 180, '高级'),
('跳绳燃脂挑战', '有氧', '1. 基础跳 1分钟\n2. 单脚跳 左右各30秒\n3. 交叉跳 1分钟\n4. 高抬腿跳 30秒\n5. 双摇跳 30秒\n6. 自由跳 2分钟', 10, 120, '中级'),
('肩部三角肌轰炸', '手臂', '1. 哑铃侧平举 15次×4组\n2. 哑铃前平举 12次×3组\n3. 俯身飞鸟 15次×3组\n4. 阿诺德推举 12次×4组\n5. 哑铃直立划船 12次×3组', 40, 220, '高级'),
('睡前舒缓拉伸', '全身', '1. 颈部拉伸 左右各15秒\n2. 肩部环绕 10次\n3. 坐姿前屈 保持30秒\n4. 蝴蝶式 保持1分钟\n5. 仰卧扭转 左右各30秒\n6. 全身放松 2分钟', 15, 40, '初级'),
('深蹲挑战100天', '腿部', '1. 标准深蹲 30次\n2. 窄距深蹲 20次\n3. 宽距深蹲 20次\n4. 跳跃深蹲 15次\n5. 静态深蹲 60秒', 20, 180, '中级'),
('背部瑜伽理疗', '背部', '1. 猫式 10次呼吸\n2. 狮身人面式 保持1分钟\n3. 蝗虫式 保持30秒×3组\n4. 桥式 保持1分钟\n5. 鱼式 保持30秒', 20, 70, '初级'),
('Tabata极限燃脂', '有氧', '1. 波比跳 20秒+休息10秒\n2. 登山跑 20秒+休息10秒\n3. 深蹲跳 20秒+休息10秒\n4. 俯卧撑 20秒+休息10秒\n5. 高抬腿 20秒+休息10秒\n6. 开合跳 20秒+休息10秒\n7. 平板支撑 20秒+休息10秒\n8. 原地冲刺 20秒+休息10秒', 4, 50, '高级'),
('完美胸型雕刻', '胸肌', '1. 哑铃卧推 12次×4组\n2. 哑铃飞鸟 12次×4组\n3. 上斜哑铃推举 12次×3组\n4. 下斜俯卧撑 15次×3组\n5. 平板哑铃推举 12次×3组', 45, 300, '高级'),
('瑜伽倒立入门', '全身', '1. 海豚式 保持30秒\n2. 前臂倒立准备 保持1分钟\n3. 靠墙倒立 保持30秒×3组\n4. 头倒立辅助 保持30秒\n5. 婴儿式放松 保持1分钟', 20, 80, '高级');

-- 用户收藏表
CREATE TABLE IF NOT EXISTS `user_favorite` (
    `id`            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `user_id`       BIGINT       NOT NULL       COMMENT '用户 ID',
    `target_type`   VARCHAR(20)  NOT NULL       COMMENT '类型: meal / workout',
    `target_id`     BIGINT       NOT NULL       COMMENT '目标 ID',
    `created_at`    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
    INDEX `idx_user_type` (`user_id`, `target_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- 每日饮食训练记录表
CREATE TABLE IF NOT EXISTS `daily_record` (
    `id`            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `user_id`       BIGINT       NOT NULL       COMMENT '用户 ID',
    `record_date`   DATE         NOT NULL       COMMENT '记录日期',
    `record_type`   VARCHAR(20)  NOT NULL       COMMENT '类型: meal / workout',
    `target_id`     BIGINT       NOT NULL       COMMENT '关联的 meal/workout ID',
    `target_name`   VARCHAR(100) DEFAULT NULL   COMMENT '冗余名称(快照)',
    `calories`      INT          DEFAULT 0      COMMENT '卡路里(摄入/消耗)',
    `created_at`    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    INDEX `idx_user_date` (`user_id`, `record_date`),
    INDEX `idx_user_date_type` (`user_id`, `record_date`, `record_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日饮食训练记录表';

-- 健康资讯/文章表
CREATE TABLE IF NOT EXISTS `article` (
    `id`            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `title`         VARCHAR(200) NOT NULL       COMMENT '文章标题',
    `summary`       VARCHAR(500) NOT NULL       COMMENT '文章摘要',
    `content`       LONGTEXT     NOT NULL       COMMENT '文章正文(Markdown)',
    `cover_emoji`   VARCHAR(10)  DEFAULT '📰'  COMMENT '封面Emoji',
    `category`      VARCHAR(30)  NOT NULL       COMMENT '分类: nutrition/training/myths/mental',
    `tags`          VARCHAR(200) DEFAULT NULL   COMMENT '标签，逗号分隔',
    `view_count`    INT          DEFAULT 0      COMMENT '阅读量',
    `created_at`    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_category` (`category`),
    INDEX `idx_created_at` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康资讯文章表';

-- 补剂指南表
CREATE TABLE IF NOT EXISTS `supplement` (
    `id`            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `name`          VARCHAR(100) NOT NULL       COMMENT '补剂名称',
    `emoji`         VARCHAR(10)  DEFAULT '💊'  COMMENT 'Emoji图标',
    `category`      VARCHAR(30)  NOT NULL       COMMENT '分类: protein/creatine/vitamin/amino/other',
    `description`   TEXT         NOT NULL       COMMENT '补剂简介',
    `benefits`      TEXT         DEFAULT NULL   COMMENT '主要功效',
    `dosage`        VARCHAR(200) DEFAULT NULL   COMMENT '推荐剂量',
    `timing`        VARCHAR(200) DEFAULT NULL   COMMENT '服用时机',
    `side_effects`  TEXT         DEFAULT NULL   COMMENT '注意事项/副作用',
    `suitable_for`  VARCHAR(200) DEFAULT NULL   COMMENT '适用人群',
    `created_at`    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补剂指南表';
