package helpers.db;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

import java.util.List;

/**
 * Created by Ovidiu on 19-May-18.
 */
public interface HibernateHlp<T> {
    public static final SessionFactory sessionFactory = buildSessionFactory();

    static SessionFactory buildSessionFactory() {
        final ServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure()
                .build();
        return new MetadataSources(registry).buildMetadata().buildSessionFactory();
    }

    public  int save(T t);
    public void delete(T t);
    public void update(T t);
    public T findById(int id);
    public List<T> fetchAll();
}
