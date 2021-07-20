package com.hrm.service.ServiceImpl;


import com.hrm.dao.ProductDao;
import com.hrm.pojo.*;
import com.hrm.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zjw
 * @package hrm
 * @Date 2021/7/15
 * @Time 18:50
 */

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDao productDao;

    @Override
    public Product findById(int product_id) {
        return productDao.findById(product_id);
    }

    @Override
    public int updateProductSurplus(Product product) {
        return productDao.updateProductSurplus(product);
    }

    @Override
    public List<Product> listProductsByType(String type) {
        return productDao.listProductsByType(type);
    }

    @Override
    public List<Product> listTrialProductsByType(String type) {
        return productDao.listTrialProductsByType(type);
    }

    @Override
    public int insertProduct(Product product) {
        return productDao.insertProduct(product);
    }

    @Override
    public int deleteProduct(Product product) {
        return productDao.deleteProduct(product);
    }

    @Override
    public int deleteProductInOtherTable(Product product) {
        return productDao.deleteProductInOtherTable(product);
    }

    @Override
    public int updateProduct(Product product) {
        return productDao.updateProduct(product);
    }

    @Override
    public List<Product> listProducts(Page page_num) {
        return productDao.listProducts(page_num);
    }

    @Override
    public ArrayList<PersonProduct> findByProductId(ID id) {
        return productDao.findByProductId(id);
    }
}
