package com.sondertara;

import com.sondertara.excel.ExcelExportTara;
import com.sondertara.excel.entity.PageQueryParam;
import com.sondertara.model.UserDTO;
import com.sondertara.model.UserInfoVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * the export demo
 * <p>
 * date 2019/12/15 9:08 下午
 *
 * @author huangxiaohu
 * @version 1.0
 * @since 1.0
 **/
public class ExcelExportDemo {
    private static final Logger logger = LoggerFactory.getLogger(ExcelExportDemo.class);

    private final AtomicInteger atomicInteger = new AtomicInteger(0);

    public void exportCsvDemo() {

        PageQueryParam query = PageQueryParam.builder().build();
        String path = ExcelExportTara.of(UserInfoVo.class).query(query, pageNo -> {

            // query list data from db
            List<UserDTO> list = new ArrayList<>(200);
            for (int i = 0; i < 200; i++) {
                UserDTO userDTO = new UserDTO();

                userDTO.setA(i);
                userDTO.setN(pageNo + "测试姓名" + i);
                userDTO.setD("测试地址" + i);
                list.add(userDTO);

                if (pageNo == 5 && i == 150) {
                    break;
                }
            }
            atomicInteger.getAndAdd(list.size());

            // convert to target data list
            return list.stream().map(u -> {
                UserInfoVo userInfoVo = new UserInfoVo();
                userInfoVo.setAddress(u.getD());
                userInfoVo.setAge(u.getA());
                userInfoVo.setName(u.getN());
                return userInfoVo;
            }).collect(Collectors.toList());

        }).exportCsv("Excel-Test");
        logger.info("path:{}", path);
        logger.info("data list size:{}", atomicInteger.get());
        //FileUtils.remove(path);
    }

    public static void main(String[] args) {
        new ExcelExportDemo().exportCsvDemo();
    }
}
