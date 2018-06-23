package helpers.db;

import model.Category;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.List;

/**
 * Created by Ovidiu on 19-May-18.
 */
public class CategoryDBHelper implements HibernateHlp<Category> {

    @Override
    public int save(Category category) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();

        int id = (int) session.save(category);

        session.getTransaction().commit();

        session.close();

        return id;
    }

    @Override
    public void delete(Category category) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();

        session.delete(category);

        session.getTransaction().commit();

        session.close();
    }

    @Override
    public void update(Category category) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();

        session.update(category);

        session.getTransaction().commit();

        session.close();

    }

    @Override
    public Category findById(int id) {
        Session session = sessionFactory.openSession();

        Category category = session.get(Category.class, id);

        session.close();

        return category;
    }

    @Override
    public List<Category> fetchAll() {
        Session session = sessionFactory.openSession();

        Criteria criteria = session.createCriteria(Category.class);

        List<Category> categories = criteria.list();

        session.close();

        return categories;
    }

    public Boolean nameExists(String name) {
        Session session = sessionFactory.openSession();
        Query query = session.createQuery("select 1 from Category c where c.name = :name");
        query.setString("name", name);
        boolean result = query.uniqueResult() != null;
        session.close();

        return result;
    }

}
