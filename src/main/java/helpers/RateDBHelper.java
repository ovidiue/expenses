package helpers;

import model.Rate;
import org.hibernate.Criteria;
import org.hibernate.Session;

import java.util.List;

/**
 * Created by Ovidiu on 27-May-18.
 */
public class RateDBHelper implements HibernateHlp<Rate> {

    @Override
    public int save(Rate rate) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();

        int id = (int) session.save(rate);

        session.getTransaction().commit();

        session.close();

        return id;
    }

    @Override
    public void delete(Rate rate) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();

        session.delete(rate);

        session.getTransaction().commit();

        session.close();
    }

    @Override
    public void update(Rate rate) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();

        session.update(rate);

        session.getTransaction().commit();

        session.close();

    }

    @Override
    public Rate findById(int id) {
        Session session = sessionFactory.openSession();

        Rate rate = session.get(Rate.class, id);

        session.close();

        return rate;
    }

    @Override
    public List<Rate> fetchAll() {
        Session session = sessionFactory.openSession();

        Criteria criteria = session.createCriteria(Rate.class);

        List<Rate> rates = criteria.list();

        session.close();

        return rates;
    }

}
