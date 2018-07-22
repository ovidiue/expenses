package helpers.repositories;

import model.Expense;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

import java.util.List;

/**
 * Created by Ovidiu on 18-May-18.
 */
public class HibernateHelper {

    public static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        final ServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure()
                .build();
        return new MetadataSources(registry).buildMetadata().buildSessionFactory();
    }

    public static void delete(Expense expense) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();

        session.delete(expense);

        session.getTransaction().commit();

        session.close();
    }

    public static Expense findContactById(int id) {
        Session session = sessionFactory.openSession();

        Expense expense = session.get(Expense.class, id);

        session.close();

        return expense;
    }

    public static void update(Expense expense) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();

        session.update(expense);

        session.getTransaction().commit();

        session.close();
    }

    @SuppressWarnings("unchecked")
    public static List<Expense> fetchAllExpenses() {
        Session session = sessionFactory.openSession();

        Criteria criteria = session.createCriteria(Expense.class);

        List<Expense> contacts = criteria.list();

        session.close();

        return contacts;
    }

    public static int save(Expense expense) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();

        int id = (int) session.save(expense);

        session.getTransaction().commit();

        session.close();

        return id;
    }

}
