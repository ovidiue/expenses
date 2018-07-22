package helpers.repositories;

import model.Tag;
import org.hibernate.Criteria;
import org.hibernate.Session;

import java.util.List;

/**
 * Created by Ovidiu on 19-May-18.
 */
public class TagRepository implements HibernateHlp<Tag> {
    @Override
    public int save(Tag tag) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();

        int id = (int) session.save(tag);

        session.getTransaction().commit();

        session.close();

        return id;
    }

    @Override
    public void delete(Tag tag) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();

        session.delete(tag);

        session.getTransaction().commit();

        session.close();
    }

    @Override
    public void update(Tag tag) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();

        session.update(tag);

        session.getTransaction().commit();

        session.close();

    }

    @Override
    public Tag findById(int id) {
        Session session = sessionFactory.openSession();

        Tag tag = session.get(Tag.class, id);

        session.close();

        return tag;
    }

    @Override
    public List<Tag> fetchAll() {
        Session session = sessionFactory.openSession();

        Criteria criteria = session.createCriteria(Tag.class);

        List<Tag> categories = criteria.list();

        session.close();

        return categories;
    }


}
