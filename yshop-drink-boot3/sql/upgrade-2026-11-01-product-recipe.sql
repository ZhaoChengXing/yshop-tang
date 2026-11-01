-- 菜品绑定菜谱与教学视频（抖音分享链接）
ALTER TABLE `yshop_store_product`
    ADD COLUMN `recipe` text NULL COMMENT '菜谱/制作步骤' AFTER `description`,
    ADD COLUMN `video_url` varchar(500) NULL COMMENT '教学视频链接（抖音分享链接）' AFTER `recipe`,
    ADD COLUMN `video_title` varchar(200) NULL COMMENT '教学视频标题' AFTER `video_url`;
